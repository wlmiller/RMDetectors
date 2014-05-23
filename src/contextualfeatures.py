# Extract features based on the contextual features
# generated by generatcontextual.py

print "Extracting feature of contextual parameters."

datafile = open("Data_All_Synced_contextual.txt")

line = datafile.readline()
featuremap = {"NA": 0}
i = 0

for feature in line.split("\t"):
    featuremap[feature] = i
    i = i + 1

outfile = open("Data_All_Synced_contextual_features.txt","w")
outfile.write(line.replace("eol\n","") + "PJtimesPG\tPJbeforetimesPS\tPGcubed\tPJcubed\tPScubed\tavgPJ\tsumPJ\tareaPJ\tpeakPJ\t2ndPeakPJ\t3rdPeakPJ\tpeakIndex\t2ndPeakIndex\t3rdPeakIndex\t2PeakDist\t2PeakRelDist\t2PeakDecr\t2PeakRelDecr\t3PeakDecr\t3PeakRelDecr\teol\n")

student = "none"
for line in datafile:
    if not (line.split("\t")[8] == student):
        student = line.split("\t")[8]
        
        sumPJ = {"NA": 0}
        areaPJ = {"NA": 0}
        peakPJ = {"NA": [0,0]}
        peakPJ2 = {"NA": [0,0]}
        peakPJ3 = {"NA": [0,0]}
        PJbefore = {"NA": 0}
        count = {"NA": 0}
        PJminus1 = 0
        PJminus2 = 0
        PGminus1 = 0
        PGminus2 = 0
        PSminus1 = 0
        PSminus2 = 0
        PJ = 0
        PS = 0
        PG = 0

    PJminus2 = PJminus1
    PJminus1 = PJ
    PJ = float(line.split("\t")[featuremap["PofJmodel"]])

    PGminus2 = PGminus1
    PGminus1 = PG
    PG = float(line.split("\t")[featuremap["PofGmodel"]])
    
    PSminus2 = PSminus1
    PSminus1 = PS
    PS = float(line.split("\t")[featuremap["PofSmodel"]])

    skill = line.split("\t")[9]
    if not skill in sumPJ:
        sumPJ[skill] = 0
        areaPJ[skill] = 0
        peakPJ[skill] = [0,0]
        peakPJ2[skill] = [0,0]
        peakPJ3[skill] = [0,0]
        count[skill] = 0
        PJbefore[skill] = 0

    sumPJ[skill] += PJ
    areaPJ[skill] += 0.5*PJbefore[skill] + 0.5*PJ
    count[skill] += 1

    if PJ > peakPJ[skill][1] or peakPJ[skill][0] == 0:
        peakPJ3[skill] = peakPJ2[skill]
        peakPJ2[skill] = peakPJ[skill]
        peakPJ[skill] = [count[skill],PJ]
    elif PJ > peakPJ2[skill][1] or peakPJ2[skill][0] == 0:
        peakPJ3[skill] = peakPJ2[skill]
        peakPJ2[skill] = [count[skill],PJ]
    elif PJ > peakPJ3[skill][1] or peakPJ3[skill][0] == 0:
        peakPJ3[skill] = [count[skill],PJ]

    line = line.replace("eol\n","")
    line += str(PJ*PG)+"\t"
    line += str(PJbefore[skill]*PS)+"\t"
    line += str(PGminus2*PGminus1*PG) + "\t"
    line += str(PJminus2*PJminus1*PJ) + "\t"
    line += str(PSminus2*PSminus1*PS) + "\t"
    line += str(sumPJ[skill]/count[skill]) + "\t"
    line += str(sumPJ[skill]) + "\t"
    line += str(areaPJ[skill]) + "\t"
    line += str(peakPJ[skill][1]) + "\t"
    line += str(peakPJ2[skill][1]) + "\t"
    line += str(peakPJ3[skill][1]) + "\t"
    line += str(peakPJ[skill][0]) + "\t"
    line += str(peakPJ2[skill][0]) + "\t"
    line += str(peakPJ3[skill][0]) + "\t"
    if peakPJ2[skill][0] != 0:
        line += str(peakPJ2[skill][0]-peakPJ[skill][0]) + "\t"
    else:
        line += "1\t"

    if peakPJ2[skill][0] != 0:
        line += str(float(peakPJ2[skill][0]-peakPJ[skill][0])/count[skill]) + "\t"
    else:
        line += "1\t"

    line += str(peakPJ[skill][1]-peakPJ2[skill][1]) + "\t"
    
    if peakPJ2[skill][0] != 0:
        line += str((peakPJ[skill][1]-peakPJ2[skill][1])/float(peakPJ2[skill][0]-peakPJ[skill][0])) + "\t"
    else:
        line += str(peakPJ[skill][1]-peakPJ2[skill][1]) + "\t"
    
    if peakPJ3[skill][0] != 0:
        line += str(peakPJ[skill][1]-peakPJ3[skill][1]) + "\t"
    else:
        line += "1\t"
    
    if peakPJ3[skill][0] != 0:
        line += str((peakPJ[skill][1]-peakPJ3[skill][1])/float(peakPJ3[skill][0]-peakPJ[skill][0])) + "\t"
    else:
        line += str(peakPJ[skill][1]-peakPJ3[skill][1]) + "\t"
    
    outfile.write(line + "eol\n")
    PJbefore[skill] = PJ

datafile.close()
outfile.close()
