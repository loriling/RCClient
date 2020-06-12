package com.elitecrm.rcclient.entity;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Loriling on 2017/2/9.
 */

public class Session {
    private long id;
    private Client client;
    private Agent firstAgent;
    private Map<String, Agent> agents = new HashMap<String, Agent>();
    private boolean robotMode = false;
    private boolean pushRating = false; //是否已经被推送过满意度

    public int getQueueId() {
        return queueId;
    }

    public void setQueueId(int queueId) {
        this.queueId = queueId;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    private int queueId;
    private String queueName;

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

    public Agent getFirstAgent() {
        return firstAgent;
    }

    public void setFirstAgent(Agent firstAgent) {
        this.firstAgent = firstAgent;
    }

    public boolean isRobotMode() {
        return robotMode;
    }

    public void setRobotMode(boolean robotMode) {
        this.robotMode = robotMode;
    }

    public boolean isPushRating() {
        return pushRating;
    }

    public void setPushRating(boolean pushRating) {
        this.pushRating = pushRating;
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
