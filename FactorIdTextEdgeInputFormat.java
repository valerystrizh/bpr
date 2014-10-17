package org.apache.giraph.examples;

import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.hadoop.io.BooleanWritable;
import org.apache.giraph.io.EdgeReader;
import org.apache.giraph.io.formats.TextEdgeInputFormat;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

/**
 * Edge reader.
 *
 * @author  Valeriya Strizhkova
 */

public class FactorIdTextEdgeInputFormat extends TextEdgeInputFormat<FactorId, BooleanWritable>
{
    private static final Pattern SEPARATOR = Pattern.compile("[\t ]");
    
    public EdgeReader<FactorId, BooleanWritable> createEdgeReader(InputSplit split, TaskAttemptContext context)
    throws IOException {
        return new TextEdgeReader();
    }
    
    public class TextEdgeReader extends TextEdgeInputFormat<FactorId, BooleanWritable>.TextEdgeReaderFromEachLineProcessed<String[]> {
        public TextEdgeReader() {
            super();
        }
        
        protected String[] preprocessLine(Text line) throws IOException {
            return FactorIdTextEdgeInputFormat.SEPARATOR.split(line.toString());
        }
        
        protected FactorId getSourceVertexId(String[] tokens) throws IOException {
            return new FactorId(Integer.parseInt(tokens[0]), false);
        }
        
        protected FactorId getTargetVertexId(String[] tokens) throws IOException {
            return new FactorId(Integer.parseInt(tokens[1]), true);
        }
        
        protected BooleanWritable getValue(String[] tokens) throws IOException {
            boolean value = Integer.parseInt(tokens[2]) == 1 ? true : false;
            return new BooleanWritable(value);
        }
    }
}