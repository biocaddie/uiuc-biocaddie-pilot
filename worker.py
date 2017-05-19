#!/usr/bin/env python

import time
import rediswq
import os
import subprocess

host = os.getenv("REDIS_SERVICE_HOST")
queue = os.getenv("REDIS_SERVICE_QUEUE")

# Ensure that our output folder exists
output_dir='/output/' + queue
if not os.path.exists(output_dir):
    os.makedirs(output_dir)

q = rediswq.RedisWQ(name=queue, host=host)
print("Worker with sessionID: " +  q.sessionID())
print("Initial queue state: empty=" + str(q.empty()))
while not q.empty():
  item = q.lease(lease_secs=10, block=True, timeout=2) 
  if item is not None:
    # Read a work item (a bash command) from the queue
    command = item.decode("utf=8")
    print("Working on {0}: {1}".format(queue, command))

    # Execute the command given by the queue item
    output = subprocess.check_output(['bash', '-c', command], shell=False)  

    # Mark the item as complete in the queue
    q.complete(item)
  else:
    print("Waiting for work from {0}".format(queue))
print("Queue " + queue + " is empty, exiting")
