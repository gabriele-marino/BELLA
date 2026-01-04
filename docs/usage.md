# Usage

## Usage Examples

### Example 1: Simple Neural Network for Sampling Rate

```xml

<changeTimes id="changeTimes" spec="RealParameter" value="0.5 1.0 1.5"/>

        <!-- Define predictors -->
<parameter id="predictor1" spec="RealParameter" value="0.1 0.5 0.8 1.2"/>

        <!-- Define weights. Dimensions are set automatically! -->
<parameter id="w1" spec="RealParameter" value="0.25"/>
<parameter id="w2" spec="RealParameter" value="0.25"/>

        <!-- Neural network with one hidden layer -->
<samplingRate id="samplingRateSV" spec="SkylineVectorParameter"
              changeTimes="@changeTimes" processLength="@origin">
<skylineValues id="samplingRate" spec="bella.BayesMLP" nodes="5">
    <predictor idref="predictor1"/>
    <weights idref="w1"/>
    <weights idref="w2"/>
    <activationFunctionHidden spec="bella.activations.ReLu"/>
    <activationFunctionsOutput spec="bella.activations.Sigmoid" upper="100.0"/>
</skylineValues>
</samplingRate>
```

### Example 2: Multi-Layer Network with Multiple Predictors

```xml
<!-- Two hidden layers with 10 and 3 nodes -->
<samplingRate spec="bella.BayesMLP" nodes="10 3">
    <!-- Multiple predictors -->
    <predictor spec="RealParameter" value="1.0 2.0 3.0 4.0"/>
    <predictor spec="RealParameter" value="0.5 1.5 2.5 3.5"/>

    <!-- Weights for each layer. BY default dimension is set automatically -->
    <weights spec="RealParameter" id="w1" value="0.25"/>  <!-- (2+1)*10 = 30 weights -->
    <weights spec="RealParameter" id="w2" value="0.25"/>  <!-- (10+1)*3 = 33 weights -->
    <weights spec="RealParameter" id="w3" value="0.25"/>  <!-- (3+1)*1 = 4 weights -->
</samplingRate>
```

### Example 3: Using with BDMM-Prime

```xml
<distribution spec="BirthDeathMigrationDistribution"
              tree="@tree" conditionOnSurvival="true">
    <parameterization spec="CanonicalParameterization" processLength="@origin">
        <birthRate spec="SkylineVectorParameter" skylineValues="@birthRate"
                   processLength="@origin"/>

        <!-- Neural network for time-varying sampling rate -->
        <samplingRate spec="SkylineVectorParameter" changeTimes="@changeTimes"
                      processLength="@origin">
            <skylineValues spec="bella.BayesMLP" nodes="10">
                <predictor spec="RealParameter" value="1.0 2.0 3.0"/>
                <weights spec="RealParameter" id="w1"/>
                <weights spec="RealParameter" id="w2"/>
            </skylineValues>
        </samplingRate>

        <deathRate spec="SkylineVectorParameter" skylineValues="@deathRate"
                   processLength="@origin"/>
        <removalProb spec="SkylineVectorParameter">
            <skylineValues spec="RealParameter" value="1.0"/>
        </removalProb>
    </parameterization>
</distribution>
```

## Weight Initialization and Priors

The dimension for a weight paramater carrying weights for a particular layer is set automatically!
Weights should be given appropriate priors. Common choices:

### Normal Prior (recomended default)
```xml
<prior id="weightsPrior" x="@w1">
    <Normal name="distr">
        <parameter spec="RealParameter" estimate="false" name="mean">0.0</parameter>
        <parameter spec="RealParameter" estimate="false" name="sigma">1.0</parameter>
    </Normal>
</prior>
```

### Laplace Prior (for sparsity)
```xml
<prior id="weightsPrior" x="@w1">
    <LaplaceDistribution name="distr">
        <parameter spec="RealParameter" estimate="false" name="mu">0.0</parameter>
        <parameter spec="RealParameter" estimate="false" name="scale">1.0</parameter>
    </LaplaceDistribution>
</prior>
```

## Operators for Weight Parameters

Use (Bactrian) random walk operator for each weight parameter:

```xml
<!-- Random walk operator -->
<operator spec="RealRandomWalkOperator" parameter="@w1"
          weight="30.0" windowSize="0.5"/>
``` 
or
```xml
<!-- Bactrian random walk operator -->
<operator spec="BactrianRandomWalkOperator" parameter="@w1"
          weight="30.0" windowSize="0.5"/>
```

## Logging

### Standard Parameter Logging

Following from previous examples where we used BELLA for sampling rate estimation:

```xml
<logger id="tracelog" fileName="$(filebase).log" logEvery="1000">
    <log idref="samplingRate"/>  <!-- Logs NN output values -->
    <log idref="w1"/>             <!-- Logs weight values -->
    <log idref="w2"/>
</logger>
```

### Weight Matrix Logging

The BayesMLP class in BELLA implements `Loggable`, allowing detailed weight logging:

```xml
<logger fileName="weights.log" logEvery="1000">
    <log spec="bella.BayesMLP" idref="samplingRate"/>
</logger>
```

This logs all weights in matrix format with headers like `W.Layer1[i][j]` where `i` is the input node index (including bias) and `j` is the output node index.

### Enhanced Tree Logging

Logger which extends TypedNodeTreeLogger to additionally log values from SkylineParameters at each node in the tree. For every node, it gets the node's age, converts it to time using Parameterization, and logs the parameter values at that time in the node metadata.

```xml

<logger spec="bella.SkylineNodeTreeLogger2"
        fileName="$(filebase).trees" logEvery="1000" tree="@tree">
    <skylineParameter idref="samplingRateSV"/>
    <parameterization idref="parameterization"/>
</logger>
```
