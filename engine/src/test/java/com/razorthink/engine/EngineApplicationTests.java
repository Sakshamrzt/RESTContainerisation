package com.razorthink.engine;

import com.razorthink.engine.util.KubernetesUtil;
import io.kubernetes.client.openapi.models.V1Pod;
import io.kubernetes.client.openapi.models.V1PodList;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
@Slf4j
class EngineApplicationTests {
	public static void main(String [] args)
	{
//		getNamesOfPods();
		getNamesOfPodsWithStatus();
	}
	public static void getNamesOfPods()
	{
		KubernetesUtil kubernetesUtil= new KubernetesUtil();
		V1PodList podList=kubernetesUtil.getPods();
		for( V1Pod item : podList.getItems() )
		{

			if( item.getMetadata() != null )
				log.info(item.getMetadata().getName());
		}
	}
	public static void getNamesOfPodsWithStatus()
	{
		KubernetesUtil kubernetesUtil= new KubernetesUtil();
		V1PodList podList=kubernetesUtil.getPods();
		for( V1Pod item : podList.getItems() )
		{

			if( item != null )
				log.info( item.getStatus().getPhase());
		}
	}



}
