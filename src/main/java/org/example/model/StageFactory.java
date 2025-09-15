package org.example.model;

import org.example.model.Systems.*;

import java.util.ArrayList;
import java.util.List;

public class StageFactory {

    public static Stage createStage1() {
        List<NetworkSystem> systems = new ArrayList<>();
        systems.add(new SourceSystem(100, 400, 8, 5));
        systems.add(new SaboteurSystem(300, 400));
        systems.add(new NetworkSystem(600, 300, 1));
        systems.add(new AntiTrojanSystem(700 , 500));
        systems.add(new SinkSystem(800, 400, 8));
        return new Stage(systems, 10000, 120, 50.0);
    }

    public static Stage createStage2() {
        List<NetworkSystem> systems = new ArrayList<>();
        systems.add(new SourceSystem(40, 200, 8, 5));
        systems.add(new NetworkSystem(250, 200, 5));
        systems.add(new NetworkSystem(250, 450, 4));
        systems.add(new NetworkSystem(500, 325, 2));
        systems.add(new NetworkSystem(700, 500, 1));
        systems.add(new SinkSystem(850, 250, 8));
        return new Stage(systems, 2000, 150, 50.0);
    }

}
