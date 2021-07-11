#!/usr/bin/env python
"""
Rwmove the bnechamrks that are not usef in the SATune studies
"""
import os

HOME_DIR = os.getenv("HOME")
BASE_DIR = f'{HOME_DIR}/satune'
BENCH_DIR = f'{BASE_DIR}/experiments/sv-comp/benchmarks/c'

samplesFile = f'{BASE_DIR}/experiments/metadata/tasks/c-tasks-all.txt'
f1 = open(samplesFile.format(taskPart),'r')
samples = f1.read().split('\n')
f1.close()

for root, _, fileNames in os.walk(BENCH_DIR):
    for fileName in fileNames:
        if fileName not in samples:
            os.remove(os.join(root, fileName))