package com.elitecrm.rcclient.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Loriling on 2017/2/9.
 */

public class Session {
    private long id;
    private Client client;
    private Map<String, Agent> agents = new HashMap<String, Agent>();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Map<String, Agent> getAgents() {
        return agents;
    }

    public void setAgents(Map<String, Agent> agents) {
        this.agents = agents;
    }

    /**
     * 获取排队候进入的第一个坐席对象
     * @return
     */
    public Agent getAgent() {
        for(String agentId : agents.keySet()){
            return agents.get(agentId);
        }
        return null;
    }

    public void addAgent(Agent agent) {
        if(agent != null){
            agents.put(agent.getId(), agent);
        }
    }

}
