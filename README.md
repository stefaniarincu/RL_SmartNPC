# SmartNPC
Reinforcement Learning Project

![Cover](https://github.com/DragosGhinea/RL_SmartNPC/blob/main/images/Cover.png)

## What is SmartNPC?
SmartNPC is a **minecraft server plugin** that creates a specialized environment for agent training and testing, hooking into the minecraft server’s APIs and implementing reinforcement learning algorithms. The current objective of the agents is to memorize and navigate a map towards a target.

## Made by

  - Dragoș-Dumitru Ghinea
  - Gabriel-Bogdan Iliescu
  - Ștefania Rîncu

## Dependencies

The plugin obviously needs a minecraft server to run, but it is hooked to other dependencies as well, and not just any type of minecraft server. We will list the dependencies below, including their purpose.

* [**AdvancedSlimeWorldManager (Server Jar + Plugin)**](https://www.spigotmc.org/resources/advanced-slimeworldmanager.87209/) - Used primarily for environment optimization, as it facilitates less disk usage, speed in world loading and no storage option, which is perfect for a temporary training environment.

* [**Citizens2 (Plugin)**](https://wiki.citizensnpcs.co/Citizens_Wiki) - The plugin responsible for handling our agents’ existence, managing the packets sent between server and client regarding artificial entities spawning (npcs).


* [**FastAsyncWorldEdit (Plugin)**](https://www.spigotmc.org/resources/fastasyncworldedit.13932/) - A world edit tool that allows us to create buildings and geometric shapes faster. Used for constructing our training maps as well as saving them as schematics which can be loaded at a later time.


* **PythonServer (Deep Learning Dependency Only)** - Included in our project files, we hook to it via sockets to train a deep q learning model. More details regarding running it will be given at a further section.

## Setup Helpers

* Starting the minecraft server:
   Enter the ProjectServer folder
   Start the slimeworld_server.jar
   You can now enter the server on localhost:25565

  (i) We usually start the server from inside the development IDE (IntelliJ) via a configuration that has the following settings:

  | Setting                 | Value                              |
  | ----------------------- | ---------------------------------- |
  | Path to jar             | Path to `slimeworld_server.jar`    |
  | VM options              |                                    |
  | Program arguments       | `--nogui`                          |
  | Working directory       | Path to the `ProjectServer` folder |
  | Environment variables   |                                    |
  | JRE                     | At least Java 17                   |


* Compiling the plugin

  The plugin is created using Maven, so you only need to execute `mvn package`, which will compile the plugin and place it inside the **ProjectServer/plugins** folder. 

  (i) The server must not be running when you compile it otherwise it won’t be able to replace the built jar inside the plugins folder!

* Running the PythonServer

  First of all you will need to create an environment that contains TensorFlow and the other dependencies the model needs to run. We provide our setup in an environment.yml file, which you can import into Conda. We used [**Anaconda Prompt**](https://docs.anaconda.com/free/anaconda/) terminal for developing and testing.

  You can follow the next steps to get the server running:

    - Open Anaconda Prompt and navigate inside PythonServer folder
    - Create the environment if you don’t have it yet using:
      `conda env create -f environment.yml`
    - Activate the environment:
       `conda activate SmartNPC`
    - Run the server script:
        `python main.py`

  (i) Start the python server before the minecraft server as the plugin will attempt to create a socket connection on load. If you opened the python server after, use the /environment reconnectPython command inside the server, before you start running the agent.

  (ii) There is no easy way of stopping the server (blame the deadline). The server accepts only one connection and stops when the peer closes it. You can also force close the terminal I guess. For this reason you can only train one deep learning agent at a time, the server does not support multiple socket connections nor concurrency of data inside the single provided one.

## Environment

(i) After starting the server, you might want to give yourself op so you can have admin privileges. You do that by writing `op yourUsername` inside the console of the minecraft server.

To start training agents you first need to initialize your world using:

 - **/environment init <numberOfAgents>** - Will start an environment session containing numberOfAgents NPCs that have the simple Q Learning algorithm set.

or

- **/environment init deep** - Will start an environment session containing one NPC that has the Deep Q Learning algorithm set.
Both environments support the following command which will start a training session:

   - **/environment train <numberOfEpisodes> <numberOfSteps>**
   - **/environment train stop**

(i) You can run this command multiple times without resetting the Q table/network of the agent.

  /environment unload - Gracefully unloads the world and agents inside it.

### Simple Q Learning specific commands:

  **/environment genetic <numberOfIterations> <numberOfEpisodes> <numberOfSteps>** - Inspired by genetic algorithms, it runs numberOfIterations training sessions. Each training session (except the first one) will have its agents start with the Q table of the best agent of the previous training session. The score based on which the best agent is selected can be modified. At the moment of writing these docs, the score is the average of the training session’s episodes reward averages. 

  **/agent showStates <AgentName>** - Very particular command that shows the memorized states by placing particles at the x,y,z coordinate of the state, and have them move in a direction, indicating the best action to take from that state.

  **/agent showStates stop** - Stops the currently active showState instance. Yes you can’t have agents named `stop`

  **/agent test <AgentName> [stop]** - Test the memorized route of an agent. The stop is an optional argument that tells the agent to stop, if it is running a testing instance.

## Evolution of the Environment

The action state has been defined and has remained unchanged as:
```
    MOVE_FORWARD,
    MOVE_BACKWARDS,
    MOVE_LEFT,
    MOVE_RIGHT
```

Same for the state definition:

```
RelativeCoordinatesState {
    x = target location’s x - agent’s location x
    y = target location’s y - agent’s location y
    z = target location’s z - agent’s location z
    direction = 
        0 if agent is facing target
        1 if target is on the left
        2 if target is on the right
        3 if agent has the target behind
}
```

### Reward

What we have changed is how the reward function is defined, and how the actions are executed.

The reward is 100 if the agent reaches a terminal state, or a cumulative negative sum otherwise, transforming the agent’s goal into a minimization problem. The agent will be penalized for how far from the target he is, if he moves away from the target, or if he doesn’t change its position from the previous step (hitting a wall).

### Executing actions

The actions have been modified three times to improve the environment.

- **Initial actions:** The agent would be pushed around using the native velocity API. It was a pretty lightweight way to execute the movements, but it introduced too many stochastic observations, since we were not able to properly detect when the action finished executing and when we had moved to the next state.


- **Walking actions:** Digging around a bit we have found that we can use the native navigator of entities for movement tasks, via the Citizens2 dependency which overrides the entity behavior with a custom implementation. Using this approach, we are even able to include promises (CompletableFuture) and wait for the action to finish, therefore transforming the stochastic observations into deterministic ones. Instead of waiting 500ms with a Thread.sleep that didn’t guarantee completion either way, we moved to an approach that takes a medium of ~200ms per action. Which means this approach is twice as fast but more resource intensive.


- **Teleport actions:** Best approach so far for speedup training, using once more the native entity teleport API from minecraft. It guarantees to execute a movement in maximum 50ms and is not even resource intensive! This allows us to execute 20 steps per second, which is **x10 faster** than the initial actions approach of 2 steps per second!


## Implementation of the Simple Q Learning Algorithm

Initially, within the Q-Learning algorithm we used constant values for Alpha (Learning rate), Gamma (Discount factor) and Epsilon (Exploration-exploitation trade off). 
Then we switched to adaptive ones, trying to include several variations of decays for the  learning rate and exploration-exploitation trade off. 

**First try:**
We chose a small value for the learning rate to encourage the agent to learn more, considering the constraints. For the discount factor and exploration-exploitation trade off values we took some values that are considered the “best choice” in practice. 

![img](https://github.com/DragosGhinea/RL_SmartNPC/blob/main/images/image1.png)

![img](https://github.com/DragosGhinea/RL_SmartNPC/blob/main/images/image2.png)

_Highest score over iterations for alpha = 0.1, gamma = 0.99 and epsilon = 0.1_

Here we used 5 generations, each one with 10 epochs and 500 steps per epoch. As you can see, we haven’t got such great scores.

![img](https://github.com/DragosGhinea/RL_SmartNPC/blob/main/images/image3.png)

_Evolution of Agent 3 over the training_

The above figure shows that based on this approach, the best agent that we had over the training reaches the target only a few times. Moreover, it is evident that the graph exhibits slow or insufficient convergence.

**Second try:**
In the second approach we used a higher learning rate, this time equal to 0.5.

![img](https://github.com/DragosGhinea/RL_SmartNPC/blob/main/images/image4.png)

_Highest score over iterations for alpha = 0.5, gamma = 0.99 and epsilon = 0.1_

![img](https://github.com/DragosGhinea/RL_SmartNPC/blob/main/images/image5.png)

_Evolution of Agent 3 over the training_

Here we kept the initial structure, and used  5 generations, each one with 10 epochs and 500 steps per epoch. As you can see from the figures above, using a higher learning rate, our best agent reaches the target several times, and also the overall score increases quicker and more uniformly.

**Third and next tries:**
For the next implementations we have decided to use some different decays for the learning rate and exploration-exploitation trade off.
We read that if you use a higher learning rate in the beginning of the training and then you gradually decrease it, the agent might learn more.

**Best -> Step decay for learning rate:**
For this solution, I set an initial learning rate equal to 0.6 and in each new epoch it decreases with a factor of 0.01, based on the current step. At first the learning rate has a higher value and decreases lower than in the last epochs. Also we used a minimum learning rate that the agent can reach. Without it, in the last epochs the agent could have a learning rate equal to 0.0, which basically means it does not learn at all. 

![img](https://github.com/DragosGhinea/RL_SmartNPC/blob/main/images/image6.png)

![img](https://github.com/DragosGhinea/RL_SmartNPC/blob/main/images/image7.png)

_Snippet of code for the implementation_

To see if the agents can improve their scores over time, for the next figures we used 40 generations, each one with 25 epochs and 500 steps per epoch. As you can see, the agents reached a plateau and could not achieve a higher score in the last 20 generations.

![img](https://github.com/DragosGhinea/RL_SmartNPC/blob/main/images/image8.png)

_Highest score over iterations for initial_learning_rate = 0.6, decrease_factor = 0.01, min_learning_rate = 0.001, gamma = 0.99 and epsilon = 0.1_

![img](https://github.com/DragosGhinea/RL_SmartNPC/blob/main/images/image9.png)

_Evolution of all agents over the training and the moments when they reached the target_

![img](https://github.com/DragosGhinea/RL_SmartNPC/blob/main/images/image10.png)

_Evolution of Agent 1 over the training_

This approach improved the number of times that the agents reached the target and also made them achieve a lower score in a shorter period of time.

![img](https://github.com/DragosGhinea/RL_SmartNPC/blob/main/images/image11.png)

_Evolution of the learning rate_

The above figure illustrates how the learning rate decreases during training. And as it can be seen it decreases slowly and stays around 0.5, the values that we first discovered that improves the agent performance.

### Failed attempts

**CosineAnnealing Learning Rate**:

We used this decay in a previous project, but we couldn’t adapt it in this project.
![img](https://github.com/DragosGhinea/RL_SmartNPC/blob/main/images/image12.png)

This decay is based on the total number of epochs, the current step, an initial learning rate and a decreasing factor. Here we encountered the problem of deciding whether the maximum number of epochs should count a product of epochs and steps per it or just the actual epochs. In both scenarios, the learning rate dropped too fast. 

![img](https://github.com/DragosGhinea/RL_SmartNPC/blob/main/images/image13.png)

_Highest score over iterations using a cosine decay for learning rate, considering max_number_of_epochs = number_epochs*steps_per_epoch_

![img](https://github.com/DragosGhinea/RL_SmartNPC/blob/main/images/image14.png)

_Evolution of all agents over the training and the moments when they reached the target_

![img](https://github.com/DragosGhinea/RL_SmartNPC/blob/main/images/image15.png)

_Evolution of Agent 2 over the training_

As it can be seen, the overall score achieved in the entire training session is not that good, but the agents still reach the target after a few iterations and quite a lot of times. I think that the learning rate decreased too quickly because of how we chose the maximum number of epochs.

![img](https://github.com/DragosGhinea/RL_SmartNPC/blob/main/images/image16.png)

_Evolution of the learning rate_

The above figure illustrates perfectly how the learning rate is mostly constant, taking the value of the minimum learning rate we declared, because otherwise it would have vanished.

**Decay for exploration-exploitation trade off**

For this approach, we used the learning rate decay mentioned above as the best solution (the one based on step decay). 
Using that implementation, we wanted to add a decay for the exploration-exploitation trade off (EPSILON), so that at the start of each epoch we encourage the agent to explore the environment and then, as the time passes, it should exploit more based on the previous actions. We tried to adapt the code used in the laboratory, but didn’t see any improvements.

![img](https://github.com/DragosGhinea/RL_SmartNPC/blob/main/images/image17.png)
![img](https://github.com/DragosGhinea/RL_SmartNPC/blob/main/images/image18.png)

_Snippet of code for the implementation_

![img](https://github.com/DragosGhinea/RL_SmartNPC/blob/main/images/image19.png)

_Highest score over iterations using a step decay for learning rate and exploration-exploitation trade off decay_

![img](https://github.com/DragosGhinea/RL_SmartNPC/blob/main/images/image20.png)

_Evolution of Agent 2 over the training_

**Conclusions**

Overall, we think that using an adaptive learning rate or exploration-exploitation trade off can improve the agent's performance. But finding the right hyperparameters require a lot of time for training and testing. And also the problem of when those values should be reset has a huge contribution on how well the chosen approach works. For us, the step decay for the learning rate was the best choice we found. 


## Implementation of the Deep Q Learning Algorithm

(i) The environment is kept in Java, as well as the exploration-exploitation trade-off (since it is a random action pick, we can avoid taking the roundtrip to the python server).

The python server receives two types of data:
- replay buffer experience add tuple (state, action, reward, next_state, done)
- next action to execute from a state

The data is transferred as json. (Better transfer protocols could be used in the future, like gRPC).

The total number of steps is counted on the server side, each time the replay buffer receives an experience it increases. Based on the current step, we periodically update the main network (every 4 steps for example) and the target network (every 100 steps for example).

We haven’t had enough time to properly test and play around with this implementation, but here is a graph of what we have achieved with it:

![img](https://github.com/DragosGhinea/RL_SmartNPC/blob/main/images/image21.png)

## Bibliography
1. https://towardsdatascience.com/the-exploration-exploitation-dilemma-f5622fbe1e82
2. https://medium.com/analytics-vidhya/learning-rate-decay-and-methods-in-deep-learning-2cee564f910b
3. https://towardsdatascience.com/q-learning-algorithm-from-explanation-to-implementation-cdbeda2ea187
4. https://hal.science/hal-02062157/document
5. https://medium.com/@aniket.tcdav/deep-q-learning-with-tensorflow-2-686b700c868b
6. https://aleksandarhaber.com/deep-q-networks-dqn-in-python-from-scratch-by-using-openai-gym-and-tensorflow-reinforcement-learning-tutorial/
7. Ștefan Iordache, Cătălina Iordache, Ciprian Păduraru (2023). _Reinforcement Learning Course_. University of Bucharest, Faculty of Mathematics and Computer Science
