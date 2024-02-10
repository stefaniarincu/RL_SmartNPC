import os
import random
from collections import deque

import numpy as np
import tensorflow as tf
from keras import Sequential
from keras.layers import Dense, PReLU
from keras.optimizers import Adam


def save_model_tf(model, model_name, folder='saves'):
    os.makedirs(folder, exist_ok=True)

    save_path = os.path.join(folder, f"{model_name}.h5")

    model.save(save_path)
    # print(f"Model saved successfully to {save_path}")


def load_model_tf(model_name, folder='saves'):
    load_path = os.path.join(folder, f"{model_name}.h5")

    if not os.path.exists(load_path):
        raise FileNotFoundError(f"Model file '{model_name}.h5' not found in '{folder}'")

    loaded_model = tf.keras.models.load_model(load_path)
    print(f"Model loaded successfully from {load_path}")

    return loaded_model


class Memory:
    def __init__(self, max_len):
        self.max_len = max_len
        self.experiences = deque(maxlen=max_len)

    def add_experience(self, state, action_taken, reward, next_state, done):
        self.experiences.append((state, action_taken, reward, next_state, done))

    def minibatch(self, size):
        return random.sample(self.experiences, size)


class Model:
    def __init__(self, input_dim, output_dim, lr, gamma, batch_size, replay_buffer_size):
        self.input_dim = input_dim
        self.output_dim = output_dim
        self.lr = lr
        self.gamma = gamma
        self.main_network = self.create()
        self.target_network = self.create()
        self.target_network.set_weights(self.main_network.get_weights())
        self.memory = Memory(replay_buffer_size)
        self.batch_size = batch_size

    def create(self):
        model = Sequential()
        model.add(Dense(32, input_dim=self.input_dim, kernel_initializer=tf.keras.initializers.HeUniform()))
        model.add(PReLU())
        model.add(Dense(64, kernel_initializer=tf.keras.initializers.HeUniform()))
        model.add(PReLU())
        model.add(Dense(32, kernel_initializer=tf.keras.initializers.HeUniform()))
        model.add(PReLU())
        model.add(Dense(self.output_dim, activation='linear', kernel_initializer=tf.keras.initializers.HeUniform()))
        model.compile(optimizer=Adam(self.lr), loss=tf.keras.losses.Huber())
        return model

    def update_target(self):
        self.target_network.set_weights(self.main_network.get_weights())
        print("Copied main network to target network")
        save_model_tf(self.target_network, "target_network_save")

    def train(self, step):
        X, Y = [], []

        if len(self.memory.experiences) >= self.batch_size:
            minibatch = self.memory.minibatch(self.batch_size)

            current_state_batch = np.array([row[0] for row in minibatch])
            next_state_batch = np.array([row[3] for row in minibatch])

            qvalue = self.main_network.predict(current_state_batch, batch_size=self.batch_size, verbose=0)
            future_qvalue = self.target_network.predict(next_state_batch, verbose=0)

            X = current_state_batch

            for index, (state, action, reward, state_, done) in enumerate(minibatch):
                if done is True:
                    Qtarget = reward
                else:
                    Qtarget = reward + self.gamma * np.max(future_qvalue[index])

                QCurrent = qvalue[index]
                QCurrent[action] = Qtarget

                Y.append(QCurrent)

            X, Y = np.array(X), np.array(Y)

            loss = self.main_network.fit(X, Y, batch_size=self.batch_size, shuffle=False, verbose=0, epochs=5)
            print(f"Step {step} loss: {loss.history['loss'][0]}")

    def select_action(self, state):
        Qvalues = self.main_network.predict(state, verbose=0)

        return np.random.choice(np.where(Qvalues[0, :] == np.max(Qvalues[0, :]))[0])


class DeepQLearning:

    def __init__(self, id):
        self.id = id
        self.buffer = []
        self.model = Model(input_dim=4, output_dim=4, lr=0.001, gamma=0.95, replay_buffer_size=1000000, batch_size=32)
        self.totalSteps = 0
        self.main_frequency = 4
        self.target_frequency = 100

    def add_to_replay_buffer(self, state, action_taken, reward, next_state, done):
        np_state = np.array([state['x'], state['y'], state['z'], state['direction']])
        np_next_state = np.array([next_state['x'], next_state['y'], next_state['z'], next_state['direction']])
        self.model.memory.add_experience(np_state, action_taken, reward, np_next_state, done)

        if self.totalSteps % self.main_frequency == 0:
            if self.totalSteps != 0:
                self.model.train(self.totalSteps)

        if self.totalSteps % self.target_frequency == 0:
            if self.totalSteps != 0:
                self.model.update_target()
        self.totalSteps += 1

    def get_next_action(self, state):
        return self.model.select_action(np.array([[state['x'], state['y'], state['z'], state['direction']]]))
