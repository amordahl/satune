#!/usr/bin/env python
"""
Remove the c benchmarks that are not used in the SATune studies to save from disk space.
"""
import os

HOME_DIR = os.getenv("HOME")
BASE_DIR = f'{HOME_DIR}/satune'
BENCH_DIR = f'{BASE_DIR}/experiments/sv-comp/benchmarks/c'

samplesFile = f'{BASE_DIR}/experiments/metadata/tasks/c-tasks-all.txt'
f1 = open(samplesFile.format(taskPart),'r')
taskPairs = [line.split() for line in f1.read().split('\n')]
f1.close()

tasks = set([pair[0] for pair in taskPairs])
properties = set([pair[1] for pair in taskPairs])

for root, _, fileNames in os.walk(BENCH_DIR):
    for fileName in fileNames:
        if fileName not in tasks or fileName not in properties:
            os.remove(os.join(root, fileName))