Bayesian Personalized Ranking from Implicit Feedback in Java
===============
####Input

TXT file where each line is represented as  
**idI idU positive**, where
- idI identifies an item,
- idU identifies a user,
- positive equals 1 if the user idU has an implicit feedback on the item idI, 0 otherwise.

####Output

TXT file where each line is represented as  
**id factors**, where
- id identifies a user or an item
- factors represent a vector of latent factors for predicting feedback. 

####Running

>hadoop jar $GIRAPH_HOME org.apache.giraph.GiraphRunner  
org.apache.giraph.examples.BPR  
-eif org.apache.giraph.examples.FactorIdTextEdgeInputFormat 
-eip $INPUT 
-op $OUTPUT 
-vof org.apache.giraph.io.formats.IdWithValueTextOutputFormat 
-ca numFactors=$NUMFACTORS  
-ca learningRate=$LEARNINGRATE  
-ca regularization=$REGULARIZATION  
-ca iterations=$ITERATIONS  
-w $WORKERS   

Where   
$GIRAPH_HOME is directory for Giraph jar file,  
$INPUT is directory for input,  
$OUTPUT is directory for output,  
$NUMFACTORS is number of factors in vectors of latent factors,  
$LEARNINGRATE, $REGULARIZATION are learning rate and regularization for gradient descent, 
$ITERATIONS is number of iterations to execute

####Literature

Steffen Rendle , Christoph Freudenthaler , Zeno Gantner , Lars Schmidt-Thieme, BPR: Bayesian personalized ranking from implicit feedback, Proceedings of the Twenty-Fifth Conference on Uncertainty in Artificial Intelligence, p.452-461, June 18-21, 2009, Montreal, Quebec, Canada.
