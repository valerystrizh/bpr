package org.apache.giraph.examples;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import org.apache.hadoop.io.WritableComparable;

/**
 * Id for vertex.
 *
 * @author  Valeriya Strizhkova
 */

public class FactorId implements WritableComparable<FactorId>
{
    private Long id;
    private boolean user;
    
    public FactorId() {
    }
    
    public FactorId(long id, boolean user) {
        this.id = Long.valueOf(id);
        this.user = user;
    }
    
    public Long getId() {
        return id;
    }
    
    public boolean isUser() {
        return user;
    }
    
    @Override
    public void readFields(DataInput input) throws IOException {
        id = Long.valueOf(input.readLong());
        user = input.readBoolean();
    }
    
    @Override
    public void write(DataOutput output) throws IOException {
        output.writeLong(id.longValue());
        output.writeBoolean(user);
    }
    
    @Override
    public int compareTo(FactorId other) {
        if (user && (!other.isUser())) {
            return 1;
        }
        else if ((!user) && other.isUser()) {
            return -1;
        }
        
        return id.compareTo((Long)other.getId());
    }
    
    @Override
    public boolean equals(Object object) {
        if (!(object instanceof FactorId)) {
            return false;
        }
        
        FactorId other = (FactorId) object;
        
        if (!id.equals(other.id)) {
            return false;
        }
        
        if (user != other.user) {
            return false;
        }
        
        return true;
    }
    
    @Override
    public int hashCode() {
        return id.hashCode() + (user ? 0 : 1);
    }
    
    @Override
    public String toString() {
        return id + " " + (user ? 0 : 1) + "";
    }
}