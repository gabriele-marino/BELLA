import numpy as np
import matplotlib.pyplot as plt
np.set_printoptions(suppress=True, precision=3)
# init rnd generator
rg = np.random.default_rng(42)


class nn_config(object):
    def __init__(self,
                 n_prior_draws=10000,
                 n_instances=100,  # e.g. number of branches and/or time bins in the tree
                 n_predictors=1,
                 n_nodes_1=2,
                 bias_node_1=1,
                 n_nodes_2=0,
                 bias_node_2=1,
                 activation_function=None,
                 ):
        self.n_prior_draws = n_prior_draws
        self.n_instances = n_instances
        self.n_predictors = n_predictors
        self.n_nodes_1 = n_nodes_1
        self.bias_node_1 = bias_node_1
        self.n_nodes_2 = n_nodes_2
        self.bias_node_2 = bias_node_2
        self.activation_function = activation_function
        self.bias_node_output = 1
        self.n_outputs = 1

    def get_data(self):
        input_data = np.zeros((self.n_instances, self.n_predictors))
        input_data[:, 0] = np.random.uniform(0, 1, self.n_instances)
        input_data[:, 1] = np.random.binomial(1, p=0.5, size=self.n_instances)
        return input_data


def softplus(z):
    return np.log(np.exp(z) + 1)

def logistic_t(z, t=0.5, k=0.75, x0=0):
    return t / (1 + np.exp(-k * (z - x0)))

def fixed_sigmoid(z, lower=0, upper=0.5):
    return lower + (upper - lower) / (1 + np.exp(-z))


def relu(z):
    return np.maximum(0, z)

def run_layer(x1, x2, activation_function=None):
    # matrix multiplication
    if x1.shape[1] == x2.shape[0]:
        z = np.dot(x1, x2)
    else:
        z = np.dot(x1, x2[1:,])
        z += x2[0,]
 
    if activation_function is not None:
        # ReLU function
        z = activation_function(z)
    return z



def sample_nn_prior(config : nn_config):
    res = []

    for _ in range(config.n_prior_draws):
        input_data = config.get_data()
        # 1st hidden layer
        weigths_l1 = rg.normal(0, 1, (config.n_predictors + config.bias_node_1, config.n_nodes_1))
        z1 = run_layer(input_data, weigths_l1, activation_function=relu) # shape: (n_instances, n_nodes 1)
    
        # 2nd hidden layer
        if config.n_nodes_2:
            weigths_l2 = rg.normal(0, 1, (z1.shape[1] + config.bias_node_2, config.n_nodes_2))
            z2 = run_layer(z1, weigths_l2, activation_function=relu) # shape: (n_instances, n_nodes 2)
        else:
            z2 = z1
    
        # output layer (e.g. trait-dependent rate
        weigths_output = rg.normal(0, 1, (z2.shape[1] + config.bias_node_output, config.n_outputs))
        y = run_layer(z2, weigths_output, activation_function=config.activation_function) # shape: (n_instances, n_outputs)
        # activation function to make resulting output positive (if needed)
    
        res.append(y)

    res = np.array(res).squeeze()
    return res



nn = nn_config(n_prior_draws=100000,
               n_instances=10,  # e.g. number of branches and/or time bins in the tree
               n_predictors=2, # samples from U[0, 1], Bin(1, 0.5)
               n_nodes_1=2,
               bias_node_1=1,
               n_nodes_2=0,
               bias_node_2=1,
               activation_function=logistic_t)


# MARGINAL PRIOR on OUTPUT
res = sample_nn_prior(nn)
plt.hist(res.flatten(), bins=100, density=True)
plt.show()

