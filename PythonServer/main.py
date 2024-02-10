import socket
import json
import deepQLearning

# id:DeepQLearning
agents = {}

# We initialize an agent because we know only one can run
agents[0] = deepQLearning.DeepQLearning(0)


def process_data_for_buffer(json_data):
    id = json_data['id']
    agent = agents.get(id, None)
    # For multi agent, not yet implemented
    # if agent is None:
    #     agent = deepQLearning.DeepQLearning(id)
    #     agents[id] = agent

    agent.add_to_replay_buffer(json_data['state'], json_data['actionTaken'], json_data['reward'],
                               json_data['nextState'], json_data['done'])


def get_next_action(id, state):
    agent = agents.get(id, None)
    # For multi agent, not yet implemented
    # if agent is None:
    #     agent = deepQLearning.DeepQLearning(id)
    #     agents[id] = agent

    return agent.get_next_action(state)


def receive_data(client_socket):
    buffer = ""

    while True:
        data = client_socket.recv(1024).decode('utf-8')

        if not data:
            break  # Connection closed

        buffer += data

        while '\n' in buffer:
            json_str, buffer = buffer.split('\n', 1)
            process_json(client_socket, json_str)


def process_json(client_socket, json_str):
    json_data = json.loads(json_str)
    if 'reward' in json_data:
        process_data_for_buffer(json_data)
    elif 'state' in json_data:
        action = get_next_action(json_data['id'], json_data['state'])
        client_socket.send(f"{action}\n".encode('utf-8'))


server_host = '127.0.0.1'
server_port = 25567

server_socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
server_socket.bind((server_host, server_port))
server_socket.listen(1)

print(f"Server listening on {server_host}:{server_port}")

try:
    client_socket, client_address = server_socket.accept()
    print(f"Connection from {client_address}")

    receive_data(client_socket)

except KeyboardInterrupt:
    print("Server interrupted. Closing the server.")
except json.JSONDecodeError as e:
    print(f"Error decoding JSON: {e}")
finally:
    server_socket.close()



