package com.gogitek.btl;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.gogitek.btl.processing.BusinessResolveService;


public class EplGAParallel {


    public static void main(String[] args) {
        final ActorSystem actorSystem = ActorSystem.create("Epl-ga-System");
        ActorRef masterActor = actorSystem.actorOf(BusinessResolveService.props());
        masterActor.tell(new BusinessResolveService.Init(), null);
    }


}
