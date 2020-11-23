package com.razorthink.engine.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.razorthink.engine.exception.ResourceNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.kubernetes.client.util.Yaml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.github.javafaker.Faker;
import io.kubernetes.client.custom.Quantity;
import io.kubernetes.client.custom.V1Patch;
import io.kubernetes.client.openapi.ApiClient;
import io.kubernetes.client.openapi.ApiException;
import io.kubernetes.client.openapi.Configuration;
import io.kubernetes.client.openapi.apis.AppsV1Api;
import io.kubernetes.client.openapi.apis.CoreV1Api;
import io.kubernetes.client.openapi.models.V1Container;
import io.kubernetes.client.openapi.models.V1ContainerPort;
import io.kubernetes.client.openapi.models.V1DeleteOptions;
import io.kubernetes.client.openapi.models.V1Deployment;
import io.kubernetes.client.openapi.models.V1DeploymentList;
import io.kubernetes.client.openapi.models.V1DeploymentSpec;
import io.kubernetes.client.openapi.models.V1LabelSelector;
import io.kubernetes.client.openapi.models.V1ObjectMeta;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import io.kubernetes.client.openapi.models.V1PodSpec;
import io.kubernetes.client.openapi.models.V1PodTemplateSpec;
import io.kubernetes.client.openapi.models.V1ResourceRequirements;
import io.kubernetes.client.openapi.models.V1Service;
import io.kubernetes.client.openapi.models.V1ServiceList;
import io.kubernetes.client.openapi.models.V1ServicePort;
import io.kubernetes.client.openapi.models.V1ServiceSpec;
import io.kubernetes.client.openapi.models.V1Status;
import io.kubernetes.client.util.ClientBuilder;
import io.kubernetes.client.util.KubeConfig;
import io.kubernetes.client.util.PatchUtils;
import com.razorthink.engine.bean.KubernetesCreateParameter;

public class KubernetesUtil {

    private static final String DEFAULT_NAME_SPACE = "default";

    final Logger log = LoggerFactory.getLogger(KubernetesUtil.class);

    private String memoryLimit = "100Mi";

    private String cpuLimit = "50m";

    private String kubeConfigPath = "/home/sakshamthakur/.kube/config";

    private AppsV1Api appsV1Api;

    public KubernetesUtil()
    {
        try
        {

            ApiClient client = ClientBuilder.kubeconfig(KubeConfig.loadKubeConfig(new FileReader(kubeConfigPath)))
                    .build();
            Configuration.setDefaultApiClient(client);
            CoreV1Api CORE_V1_API = new CoreV1Api(client);
            this.appsV1Api = new AppsV1Api();
            this.appsV1Api.setApiClient(CORE_V1_API.getApiClient());
        }
        catch( IOException e )
        {
            log.error("Error while creating api client", e);
        }
    }

    public void createDeployment( KubernetesCreateParameter inputParameter)
    {
        V1Deployment deployment = new V1Deployment();

        try
        {
            V1ObjectMeta deploymentMetadata = buildDeploymentMetaData(inputParameter.getDeploymentName());

            V1DeploymentSpec deploymentSpec = buildDeploymentSpec(inputParameter);

            deployment.setApiVersion("apps/v1");
            deployment.setKind("Deployment");
            deployment.setMetadata(deploymentMetadata);
            deployment.setSpec(deploymentSpec);
            System.out.println(Yaml.dump(deployment));
            V1Deployment result = appsV1Api.createNamespacedDeployment("default", deployment, null, null, null);
            log.info("Deployment created:{0}", result);
        }
        catch( ApiException exception )
        {
            log.error("Api Exception occurred while creating deployment object", exception);
            throw new ResourceNotFoundException("Api Exception occurred while creating deployment object");
        }
    }

    public void createService( String serviceName, String namespace, List<Integer> portList )
    {
        try
        {
            V1Service ser = new V1Service();

            ser.setKind("Service");
            ser.setApiVersion("v1");
            Faker faker = new Faker();

            String name = faker.name().firstName();

            V1ObjectMeta metaData = new V1ObjectMeta();
            metaData.setName(serviceName);
            ser.setMetadata(metaData);

            V1ServiceSpec spec = new V1ServiceSpec();
            spec.setType("LoadBalancer");
            Map<String, String> mp = new HashMap<>();
            String selectorName=getSelector(serviceName);//+"-deployment";
            mp.put("app", selectorName);
            spec.setSelector(mp);

            List<V1ServicePort> ports = new ArrayList<>();
            //            for( Integer port : portList )
            //            {
            ports.add(new V1ServicePort().port(8080).name(name.toLowerCase()));
            //            }
            spec.setPorts(ports);
            ser.setSpec(spec);
            //            //system.out.println(Yaml.dump(ser));
            CoreV1Api apiCore = new CoreV1Api();

            V1Service result = apiCore.createNamespacedService(DEFAULT_NAME_SPACE, ser, null, null, null);
            log.info("Service created:{0}", result);
        }
        catch( Exception exception )
        {
            log.error(" Exception occurred while creating service :   ", exception);
            //system.out.println(exception);
        }
    }

    public void deleteDeployment( String deploymentName )
    {
        final V1Deployment deploymentObject = getDeploymentObjectFromName(deploymentName);

        if( deploymentObject == null )
            return;

        final String namespace = deploymentObject.getMetadata().getNamespace();

        V1DeleteOptions body = new V1DeleteOptions();
        try
        {
            V1Status result = appsV1Api.deleteNamespacedDeployment(deploymentName, namespace, null, null, 20, false,
                    "Background", body);
            log.info("Deleted deployment :{}", result);
        }
        catch( ApiException exception )
        {
            log.error("Exception occurred while deleting deployment :  ", exception);
        }
        catch( NullPointerException exception )
        {
            log.error("Null Pointer Exception occurred while deleting deployment :   ", exception);
        }
    }

    public void deleteService( String serviceName, String namespace )
    {
        CoreV1Api apiCore = new CoreV1Api();

        final String fieldSelector = "metadata.name=" + serviceName;
        try
        {
            V1ServiceList serviceList = apiCore.listNamespacedService(namespace, null, null, null, fieldSelector, null,
                    null, null, null, null);
            for( V1Service item : serviceList.getItems() )
            {

                V1Status result = apiCore.deleteNamespacedService(item.getMetadata().getName(), namespace, null, null,
                        20, false, null, null);
                log.info("Deleted Service :{}", result);
            }
        }
        catch( ApiException exception )
        {
            log.error("Api Exception occurred while deleting service", exception);
        }

    }

    public void pauseOrResumeDeployment( String status, String deploymentName, String namespace )
    {

        final String jsonPatchStr;

        if( status.equals("pause") )
            jsonPatchStr = "[{\"op\":\"replace\",\"path\":\"/spec/paused\",\"value\":true}]";
        else
            jsonPatchStr = "[{\"op\":\"replace\",\"path\":\"/spec/paused\",\"value\":false}]";

        try
        {
            V1Deployment result = PatchUtils.patch(
                    V1Deployment.class, () -> appsV1Api.patchNamespacedDeploymentCall(deploymentName, "default",
                            new V1Patch(jsonPatchStr), null, null, null, null, null),
                    V1Patch.PATCH_FORMAT_JSON_PATCH, appsV1Api.getApiClient());

            log.info("Successfully paused/resumed deployment : {}", result);
        }
        catch( ApiException exception )
        {
            log.error("ApiException occurred while pausing/resuming deployment :  ", exception);
        }
        catch( NullPointerException exception )
        {
            log.error("Null Pointer Exception occurred while pausing/resuming deployment :  ", exception);
        }
    }

    private V1Deployment getDeploymentObjectFromName( String deploymentName )
    {
        try
        {
            final V1DeploymentList deploymentList = appsV1Api.listDeploymentForAllNamespaces(null, null, null, null,
                    null, null, null, null, null);

            for( V1Deployment object : deploymentList.getItems() )
            {
                if( object.getMetadata().getName().equals(deploymentName) )
                {
                    return object;
                }
            }
        }
        catch( ApiException exception )
        {
            log.error("Api Exception occurred while deleting getting deployment object :   ", exception);
        }
        catch( NullPointerException exception )
        {
            log.error("Null Pointer Exception occurred while  getting deployment object :   ", exception);
        }
        return null;
    }

    private V1ObjectMeta buildDeploymentMetaData( String deploymentName )
    {
        final V1ObjectMeta deploymentMetadata = new V1ObjectMeta();
        deploymentMetadata.setName(deploymentName);
        return deploymentMetadata;
    }

    private V1DeploymentSpec buildDeploymentSpec( KubernetesCreateParameter  parameter )
    {
        final V1DeploymentSpec deploymentSpec = new V1DeploymentSpec();
        deploymentSpec.setReplicas(1);

        final V1PodTemplateSpec deploymentSpecTemplate = new V1PodTemplateSpec();
        final V1PodSpec deploymentSpecTemplateSpec = new V1PodSpec();
        final V1ObjectMeta deploymentSpecTemplateMetaData = new V1ObjectMeta();
        final V1Container container = new V1Container();

        container.setImage(parameter.getImage());
        container.setImagePullPolicy("IfNotPresent");
        container.setName(parameter.getContainerName());

        V1ResourceRequirements resources = new V1ResourceRequirements();
        Map<String, Quantity> resourceLimitMap = new HashMap<>();

        resourceLimitMap.put("memory", Quantity.fromString(parameter.getMemory()));
        resourceLimitMap.put("cpu", Quantity.fromString(parameter.getCpu()));
        resources.setLimits(resourceLimitMap);
        container.setResources(resources);

        List<V1ContainerPort> portList = new ArrayList<>();
//        for( Integer port : portList )
//        {
//            V1ContainerPort containerPort = new V1ContainerPort();
//            containerPort.setContainerPort(port);
//            portList.add(containerPort);
//        }
        container.ports(portList);

        final V1LabelSelector selector = new V1LabelSelector();
        Map<String, String> matchLabel = new HashMap<>();

        matchLabel.put("app", parameter.getContainerName());

        selector.setMatchLabels(matchLabel);

        deploymentSpecTemplateMetaData.setLabels(matchLabel);
        deploymentSpecTemplateSpec.addContainersItem(container);
        deploymentSpecTemplate.setSpec(deploymentSpecTemplateSpec);
        deploymentSpecTemplate.setMetadata(deploymentSpecTemplateMetaData);
        deploymentSpec.setTemplate(deploymentSpecTemplate);
        deploymentSpec.setSelector(selector);

        return deploymentSpec;
    }

    public V1PodList getPods()
    {
        CoreV1Api apiCore = new CoreV1Api();
        int size = 0;
        V1PodList v1PodList=null;
        try
        {
            v1PodList = apiCore.listNamespacedPod(DEFAULT_NAME_SPACE, null, null, null, null, null, null, null,
                    null, null);

        }
        catch( ApiException exception )
        {
            log.error("Api Exception occurred while getting pods :   ", exception);
        }
        catch( NullPointerException exception )
        {
            log.error("Null Exception occurred while getting pods :   ", exception);
        }
        return v1PodList;
    }
    public String getPodIPUsingDeployment(String deploymentName)
    {
        CoreV1Api apiCore = new CoreV1Api();
        deploymentName= getSelector(deploymentName);
        String ip=null;
        try
        {
            V1PodList podsList = apiCore.listNamespacedPod(DEFAULT_NAME_SPACE, null, null, null, null, null, null, null,
                    null, null);
            List<String> names = new ArrayList<>();

            for( V1Pod item : podsList.getItems() )
            {
                if(getSelector(item.getMetadata().getName()).equals(deploymentName))
                {
                    ip=item.getStatus().getPodIP();
                    break;
                }
            }
        }
        catch( ApiException exception )
        {
            log.error("Api Exception occurred while getting pods :   ", exception);
        }
        catch( NullPointerException exception )
        {
            log.error("Null Exception occurred while getting pods :   ", exception);
        }
        return ip;
    }

    public String getServicePort( String deploymentName )
    {
        CoreV1Api apiCore = new CoreV1Api();
        try
        {
            final String fieldSelector = "metadata.name=" + deploymentName;
            final V1ServiceList serviceList = apiCore.listNamespacedService("default", null, null, null, fieldSelector,
                    null, null, null, null, null);

            if( serviceList.getItems().size() == 0 )
                return null;

            final V1Service service = serviceList.getItems().get(0);
            return (service.getStatus().getLoadBalancer().getIngress().get(0).getIp());
        }
        catch( ApiException exception )
        {
            log.error("Api Exception occurred while deleting getting deployment object :   ", exception);
        }
        catch( NullPointerException exception )
        {
            log.error("Null Pointer Exception occurred while  getting deployment object :   ", exception);
        }
        return null;
    }
    String getSelector(String serviceName)
    {
        String newName="";
        for(int i=0;i<serviceName.length();i++)
        {
            Character atI=serviceName.charAt(i);
            if(atI.equals('-'))
                break;
            else
                newName+=atI;
        }
        return newName;
    }

    public void updateResources( String deploymentName, String namespace, String memory, String cpu )
    {
        final Map<String,String> map = new HashMap<>();
        map.put("op","replace");
        map.put("path","/spec/template/spec/containers");///spec/template/spec/containers/resources/limits/memory
        map.put("value","null");
        Gson gson = new GsonBuilder().disableHtmlEscaping().create();
        final String jsonPatchStr = gson.toJson(map);
        System.out.println(jsonPatchStr);
        final ObjectMapper objectMapper = new ObjectMapper();

        try
        {
            V1Deployment result = PatchUtils.patch(
                    V1Deployment.class, () -> appsV1Api.patchNamespacedDeploymentCall(deploymentName, "default",
                            new V1Patch(jsonPatchStr), null, null, null, null, null),
                    V1Patch.PATCH_FORMAT_JSON_PATCH, appsV1Api.getApiClient());

            log.info("Successfully updated resources : {}", result);
        }
        catch( ApiException exception )
        {
            log.error("ApiException occurred while pausing/resuming deployment :  ", exception);
        }
        catch( NullPointerException exception )
        {
            log.error("Null Pointer Exception occurred while pausing/resuming deployment :  ", exception);
        }
    }
}