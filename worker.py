#!/usr/bin/env python

import logging
import time
import rediswq
import sys
import os
import subprocess

host = os.getenv("REDIS_SERVICE_HOST")
queue = os.getenv("REDIS_SERVICE_QUEUE")

# Ensure that our output folder exists
output_dir='/output/' + queue.replace('-', '/')
if not os.path.exists(output_dir):
    os.makedirs(output_dir)

# create formatter and add it to the log handlers
formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')

# Set up a console logger
logger = logging.getLogger(queue)
logger.setLevel(logging.DEBUG)
ch = logging.StreamHandler()
ch.setLevel(logging.INFO)
ch.setFormatter(formatter)

# create formatter and add it to the handlers
logger.addHandler(ch)

q = rediswq.RedisWQ(name=queue, host=host)
logger.info("Worker started with sessionID: {0}".format(q.sessionID()))
logger.debug("Initial state: empty={0}".format(str(q.empty())))

start_time = time.time()
while not q.empty():
  item = q.lease(lease_secs=10, block=True, timeout=2) 
  if item is not None:
    # Read a work item (a bash command) from the queue
    command = item.decode("utf=8")
    logger.info("Running command: {0}".format(command))

    # Execute the command given by the queue item
    output = subprocess.check_output(['bash', '-c', command], shell=False) 

    # Mark the item as complete in the queue
    q.complete(item)
  else:
    logger.debug("Waiting for work")
logger.debug("Queue is empty, exiting")

# Calculate elapsed run time
elapsed_time=time.time() - start_time
logger.info("Worker finished in {0:.3f} seconds".format(elapsed_time))
