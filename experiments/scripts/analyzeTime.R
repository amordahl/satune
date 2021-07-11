library(car)
library(plotrix)
library(ggplot2)
library(doBy)
library(plyr)
library(tidyr)
library(dplyr) 
library(Hmisc)
library(pgirmess)
library(xtable)
library(fmsb)
library(reshape2)
library(wesanderson)
library(ggrepel)

dir <- "/Users/ugurko/satune/experiments/satune-stats/";

# Fig.3 : Execution time of SATUNE and the baselines.
#label,task,result,cost,score,time,configCount,revisitCount,acceptCount,bestCount,saRunCount,searchAlg,tool,pca,thresh,seed,mode,improve
orig_data <- read.table(paste(dir, 'stats-time-all.csv', sep=""), header=TRUE, sep=",")
orig_data<- orig_data[ , c("label", "task", "time", "tool", "searchAlg", "thresh", "seed", "mode")]
orig_data$label <- factor(orig_data$label, levels = c("TN", "TP", "FN", "FP", "UNK"))
orig_data$tool <- factor(orig_data$tool, levels = c("CBMC", "Symbiotic", "JayHorn", "JBMC"))
orig_data$thresh <- as.factor(orig_data$thresh)
orig_data <- orig_data[orig_data$mode=='base-classification',] # Filtering legacy data
orig_data <- orig_data[orig_data$searchAlg!='bestprecision',] # bestprecision is not a baseline we compare agaist
all_data <- orig_data[(orig_data$thresh==1.0 | orig_data$thresh==-1),] # Filtering legacy data. threahold is not included in the paper/study.

plot_data <- all_data[all_data$label!='UNK',]
plot_data$time <- plot_data$time/60.0
plot_data <- ddply(plot_data, .(tool, task, searchAlg, thresh), summarise, time=median(time))
plot_data$searchAlg <- factor(plot_data$searchAlg, levels = c("SATune", "precise→random", "precise→correct", "most-correct-config"))
means <- ddply(plot_data, .(tool, searchAlg), summarise, time=mean(time))
maxs <- ddply(plot_data, .(tool, searchAlg), summarise, time=max(time))
g <- ggplot(plot_data, aes(x = searchAlg, y = time, colour=thresh))+
  geom_boxplot(outlier.size = 0.2, varwidth = TRUE) + facet_grid(.~tool, scales="free") +
  coord_trans(y = "log10") +
  scale_colour_manual(values=c("#999999", "#0072B2")) +
  scale_y_continuous(breaks = c(1, 4, 16, 32)) +
  geom_text(data = maxs , aes(label = round(time, 1), y=time), size=4, colour="red", hjust=0, vjust=-0.5) +
  geom_point(data = maxs , aes(y =time), colour = "red", size=2, shape=19, position=position_dodge(.8)) +
  theme(legend.position = "none", legend.title = element_blank(), axis.text = element_text(size = 11),
        axis.text.x = element_text(angle = 45, vjust = 0.95, hjust=0.85),
        axis.title.y = element_text(vjust = -1, hjust=0.3),
        axis.title.x = element_blank()) +
  ylab("Task completion time in minutes (log)")
print(g)
dev.print(device=postscript, file= "/Users/ugurko/Desktop/boxplot-time.eps")
dev.off()