#!/usr/bin/env python
"""
IMPORTANT!!! This script will not actually run Symbiotic (due to a problem in the paths when using subprocess).
Instead it will write the command to be run into the commands-to-run.sh file in this directory.
Thus, as a follow up step, make the commands-to-run.sh executable (i.e. chmod +x commands-to-run.sh) and execute it 
to generate the ground-truth dataset for Symboitic.
"""
import sys,os
import subprocess as sp

BASE_DIR='{}/satune'.format(str(os.getenv("HOME")))
BENCH_DIR='{}/experiments/sv-comp/benchmarks/c'.format(BASE_DIR)

f1 = open('cache.txt','r')
lines = f1.read().split('\n')
f1.close()
completedSet = set()
for l in lines:
    completedSet.add(l)

def runPart(part):
    configsFile='Configs-16opts-t3-CA.txt'
    samplesFile='{}/experiments/metadata/tasks/c-tasks-p{}.txt'.format(BASE_DIR, part)
    f1 = open(samplesFile,'r')
    samples = f1.read().split('\n')
    f1.close()
    f2 = open(configsFile,'r')
    configs = f2.read().split('\n')
    f2.close()

    f = open("commands-to-run.sh".format(part), "w")
    f.write('#!/bin/bash\n\n')
    configFixPart = ['--timeout=60','--witness','witness.graphml','--no-integrity-check']
    for sample in samples:
        srcFile, probFile = ['{}/{}'.format(BENCH_DIR, item) for item in sample.split(',')]
        probFile = '--prp='+probFile 
        for config in configs:
            dp = config + ',' + sample
            if dp in completedSet:
                continue
            cmd = 'symbiotic/bin/symbiotic {} {} {} {}'.format(probFile, configFixPart, config, srcFile)
            f.write(cmd+'\n')
            f.flush()
    f.close()

if __name__ == '__main__':
    taskPartIN = sys.argv[1]
    runPart(taskPartIN)
