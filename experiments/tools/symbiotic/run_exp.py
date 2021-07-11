#!/usr/bin/env python3.5

import sys,re,os
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
    configsFile='Configs-16opts-t3-CA.txt'.format(configPart)
    samplesFile='{}/experiments/metadata/tasks/c-tasks-p{}.txt'.format(BASE_DIR, taskPart)
    f1 = open(samplesFile.format(taskPart),'r')
    samples = f1.read().split('\n')
    f1.close()
    f2 = open(configsFile,'r')
    configs = f2.read().split('\n')
    f2.close()

    f = open("results/log-ground-truth-p{}.txt".format(part), "w")
    configFixPart = ['--timeout=60','--witness','witness.graphml','--no-integrity-check']
    for sample in samples:
        srcFile, probFile = ['{}/{}'.format(BENCH_DIR, item) for item in sample.split(',')]
        probFile = '--prp='+probFile 
        for config in configs:
            dp = config + ',' + sample
            if dp in completedSet:
                continue
            cmd = ['symbiotic/bin/symbiotic', probFile] + configFixPart + config.split(' ')
            cmd.append(srcFile)
            try:
                outs = sp.call(cmd, stdout=f, stderr=f)
                f.write(dp + ',' + str(outs)+'\n')
            except sp.TimeoutExpired:
                f.write(dp + ',' + str(outs)+ ',timeout\n')
            except:
                f.write(dp + ',' + str(outs)+ ',incomplete\n')
            f.flush()
    f.close()

if __name__ == '__main__':
    taskPartIN = sys.argv[1]
    configPartIN = sys.argv[2]
    runPart(taskPartIN, configPartIN)