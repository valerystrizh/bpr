package org.apache.giraph.examples;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Arrays;
import java.util.Random;

import org.apache.hadoop.io.Writable;

/**
 * Matrix for latent factors.
 *
 * @author  Valeriya Strizhkova
 */

public class FactorMatrix implements Writable
{
    public static int length;
    
    private float[] factors = null;
    private boolean positive = true;
    private boolean init = false;
    
    private FactorId sourceFactorId = new FactorId(-1, true);
    
    public FactorMatrix() {
        factors = new float[length];
    }
    
    public FactorMatrix(FactorMatrix factorMatrix) {
        factors = factorMatrix.factors;
    }
    
    public FactorMatrix(FactorId sourceFactorId, FactorMatrix factorMatrix, boolean positive) {
        factors = factorMatrix.factors;
        this.sourceFactorId = sourceFactorId;
        this.positive = positive;
    }
    
    public boolean isPositive() {
        return positive;
    }
    
    public boolean isInit() {
        return init;
    }
    
    public FactorId getSourceFactorId() {
        return sourceFactorId;
    }
    
    public void init() {
        Random random = new Random();
        factors = new float[length];
        init = true;
        
        for (int i = 0; i < length; ++i) {
            factors[i] = (float) random.nextFloat();
        }
    }
    
    public float get(int i) {
        return factors[i];
    }
    
    public void set(int index, float value) {
        factors[index] = value;
    }
    
    public void add(FactorMatrix factorMatrix) {
        for (int i = 0; i < length; ++i) {
            factors[i] += factorMatrix.get(i);
        }
    }
    
    public void sub(FactorMatrix factorMatrix) {
        for (int i = 0; i < length; ++i) {
            factors[i] -= factorMatrix.get(i);
        }
    }
    
    public void mul(FactorMatrix factorMatrix) {
        for (int i = 0; i < length; ++i) {
            factors[i] *= factorMatrix.get(i);
        }
    }
    
    public float sum(int index) {
        float result = 0.0f;
        
        for (int i = index; i < length; ++i) {
            result += factors[i];
        }
        
        return result;
    }
    
    @Override
    public void readFields(DataInput input) throws IOException {
        byte[] temp = new byte[length * 4];
        input.readFully(temp);
        FloatBuffer floatBuffer = ByteBuffer.wrap(temp).asFloatBuffer();
        factors = new float[length];
        floatBuffer.get(factors);
        positive = input.readBoolean();
        init = input.readBoolean();
        sourceFactorId = new FactorId();
        sourceFactorId.readFields(input);
    }
    
    @Override
    public void write(DataOutput output) throws IOException {
        byte[] temp = new byte[length * 4];
		FloatBuffer floatBuffer = ByteBuffer.wrap(temp).asFloatBuffer();
        floatBuffer.put(factors);
		output.write(temp);
        output.writeBoolean(positive);
        output.writeBoolean(init);
        sourceFactorId.write(output);
    }
    
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof FactorMatrix)) {
            return false;
        }
        
        FactorMatrix other = (FactorMatrix) object;
        
        for (int i = 0; i < length; ++i) {
            if (factors[i] != other.factors[i]) {
                return false;
            }
        }
        
        if (positive != other.positive) {
            return false;
        }
        
        if (!sourceFactorId.equals(other.sourceFactorId)) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public int hashCode() {
        return Arrays.hashCode(factors) + (positive ? 0 : 1) + sourceFactorId.hashCode();
    }
    
    @Override
    public String toString() {
        String result = "";
        
        for (int i = 0; i < length; ++i) {
            result += get(i);
            if (i < length - 1) {
                result += " ";
            }
        }
        
        return result;
    }
}