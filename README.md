# DCF
A distributed computation framework

TopologyBuilder builder = new TopologyBuilder();

builder.setEmitter(stage1, PiStage1.class.getName());
builder.setGear(stage2, PiStage2.class.getName()).meanGrouping(stage1, "shard");
builder.setGear(stage3, PiStage3.class.getName()).globalGrouping(stage2, "result");

Topology topo = builder.createTopology();
