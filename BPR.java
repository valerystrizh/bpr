package org.apache.giraph.examples;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.apache.giraph.edge.Edge;
import org.apache.giraph.graph.BasicComputation;
import org.apache.giraph.graph.Vertex;
import org.apache.hadoop.io.BooleanWritable;

/**
 * Bayesian Personalized Ranking Algorithm for Apache Giraph.
 *
 * @author  Valeriya Strizhkova
 */

public class BPR extends BasicComputation<FactorId, FactorMatrix, BooleanWritable, FactorMatrix>
{
    long iterations;
    int numFactors;
    float learningRate;
    float regularization;
    boolean parameterised = false;
    
    public void compute(Vertex<FactorId, FactorMatrix, BooleanWritable> vertex, Iterable<FactorMatrix> messages) throws IOException {
        long iteration = getSuperstep() / 3;
        setCustomArguments();
        
        if (iteration < iterations) {
            
            init(vertex, messages);
            
            if (getSuperstep() % 3 == 0 && !(((FactorId)vertex.getId()).isUser())) {
                sendMessageToUser(vertex, messages);
            }
            else if (getSuperstep() % 3 == 1 && (((FactorId)vertex.getId()).isUser())) {
                updateUser(vertex, messages);
            }
            else if (getSuperstep() % 3 == 2 && !(((FactorId)vertex.getId()).isUser())) {
                updateItem(vertex, messages);
            }
        }
        
        vertex.voteToHalt();
    }
    
    void setCustomArguments() {
        if (!parameterised) {
            iterations = Long.parseLong(getConf().get("iterations"));
            numFactors = Integer.parseInt(getConf().get("numFactors"));
            learningRate = Float.parseFloat(getConf().get("learningRate"));
            regularization = Float.parseFloat(getConf().get("regularization"));
            
            FactorMatrix.length = numFactors;
            
            parameterised = true;
        }
    }
    
    void init(Vertex<FactorId, FactorMatrix, BooleanWritable> vertex, Iterable<FactorMatrix> messages) {
        if (!((FactorMatrix)vertex.getValue()).isInit()) {
            ((FactorMatrix)vertex.getValue()).init();
            
            if (((FactorId)vertex.getId()).isUser()) {
                ((FactorMatrix)vertex.getValue()).set(0, 0.0f);
            }
        }
    }
    
    void sendMessageToUser(Vertex<FactorId, FactorMatrix, BooleanWritable> vertex, Iterable<FactorMatrix> messages) {
        for (Edge<FactorId, BooleanWritable> edge : vertex.getEdges()) {
            if (edge.getValue().get()) {
                FactorMatrix factorMatrixPositive = new FactorMatrix((FactorId)vertex.getId(), (FactorMatrix)vertex.getValue(), true);
                sendMessage(edge.getTargetVertexId(), factorMatrixPositive);
            } else {
                FactorMatrix factorMatrixNegative = new FactorMatrix((FactorId)vertex.getId(), (FactorMatrix)vertex.getValue(), false);
                sendMessage(edge.getTargetVertexId(), factorMatrixNegative);
            }
        }
    }
    
    void updateUser (Vertex<FactorId, FactorMatrix, BooleanWritable> vertex, Iterable<FactorMatrix> messages) {
        
        Random random = new Random();
        List<FactorMatrix> idsPositive = new ArrayList<FactorMatrix>();
        List<FactorMatrix> idsNegative = new ArrayList<FactorMatrix>();
        
        for (FactorMatrix factorMatrix : messages) {
            if (factorMatrix.isPositive()) {
                idsPositive.add(factorMatrix);
            } else {
                idsNegative.add(factorMatrix);
            }
        }
        
        Collections.shuffle(idsPositive);
        Collections.shuffle(idsNegative);
        
        FactorMatrix u = (FactorMatrix)vertex.getValue();
        FactorMatrix i = idsPositive.get(0);
        FactorMatrix j = idsNegative.get(0);
        
        float xUIJ = xUIJ(u, i, j);
        
        float sigmoid = 1.0f / (1.0f + (float)Math.exp(xUIJ));
        
        float iBias = learningRate * (sigmoid - regularization * i.get(0));
        float jBias = learningRate * (- sigmoid - regularization * j.get(0));
        
        FactorMatrix uUpdate = new FactorMatrix();
        FactorMatrix iUpdate = new FactorMatrix();
        FactorMatrix jUpdate = new FactorMatrix();
        
        uUpdate.set(0, 0.0f);
        iUpdate.set(0, iBias);
        jUpdate.set(0, jBias);
        
        for (int factor = 1; factor < numFactors; ++factor) {
            float wUF = u.get(factor);
            float hIF = i.get(factor);
            float hJF = j.get(factor);
            uUpdate.set(factor, learningRate * ((hIF - hJF) * sigmoid - iBias * wUF));
            iUpdate.set(factor, learningRate * (wUF * sigmoid - regularization * hIF));
            jUpdate.set(factor, learningRate * (- wUF * sigmoid - regularization * hJF));
        }
        
        update(uUpdate, vertex);
        sendMessage(i.getSourceFactorId(), iUpdate);
        sendMessage(j.getSourceFactorId(), jUpdate);
        
        for (FactorMatrix factorMatrix : messages) {
            sendMessage(factorMatrix.getSourceFactorId(), new FactorMatrix());
        }
    }
    
    float xUIJ(FactorMatrix u, FactorMatrix i, FactorMatrix j) {
        
        float sum = 0.0f;
        
        FactorMatrix result = new FactorMatrix(i);
        
        result.sub(j);
        result.mul(u);
        sum += result.sum(1);
        
        sum += i.get(0);
        sum -= j.get(0);
        
        return sum;
    }
        
    void updateItem(Vertex<FactorId, FactorMatrix, BooleanWritable> vertex, Iterable<FactorMatrix> messages) {
        for (FactorMatrix factorMatrix : messages) {
            update(factorMatrix, vertex);
            sendMessage(factorMatrix.getSourceFactorId(), new FactorMatrix());
        }
    }
        
    void update(FactorMatrix updateFactorMatrix, Vertex<FactorId, FactorMatrix, BooleanWritable> vertex) {
        ((FactorMatrix)vertex.getValue()).add(updateFactorMatrix);
    }
}