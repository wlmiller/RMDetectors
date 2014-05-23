# This script calculates P(J) and contextual guess/slip parameters.
# The coefficients of the linear regression are in "PofJ.txt" and so on.

print "Calculating contextual parameters."

PofJ = {"intercept" : 0}
modelfile = open("PofJ.txt")
for line in modelfile:
    if " * " in line:
        number = float(line.split(" * ")[0].replace(" ",""))
        feature = line.split(" * ")[1].replace("\n","")
        PofJ[feature] = number
    else:
        PofJ["intercept"] = float(line.replace(" ",""))
        break
modelfile.close()

PofG = {"intercept" : 0}
modelfile = open("PofG.txt")
for line in modelfile:
    if " * " in line:
        number = float(line.split(" * ")[0].replace(" ",""))
        feature = line.split(" * ")[1].replace("\n","")
        PofG[feature] = number
    else:
        PofG["intercept"] = float(line.replace(" ",""))
        break
modelfile.close()

PofS = {"intercept" : 0}
modelfile = open("PofS.txt")
for line in modelfile:
    if " * " in line:
        number = float(line.split(" * ")[0].replace(" ",""))
        feature = line.split(" * ")[1].replace("\n","")
        PofS[feature] = number
    else:
        PofS["intercept"] = float(line.replace(" ",""))
        break
modelfile.close()

datafile = open("Data_All_Synced.txt")

line = datafile.readline()
featuremap = {"NA": 0}
i = 0

for feature in line.split("\t"):
    featuremap[feature] = i
    i = i + 1 

outfile = open("Data_All_Synced_contextual.txt","w")
outfile.write(line.replace("eol\n","") + "PofJmodel\tPofGmodel\tPofSmodel\teol\n")
for line in datafile:
    PJ = 0
    PG = 0
    PS = 0
    for feature in PofJ:
        if feature == "intercept":
            PJ = PJ + PofJ[feature]
        else:
            number = line.split("\t")[featuremap[feature]]
            if number == ".":
                number = "0"
            PJ = PJ + PofJ[feature]*float(number)
    for feature in PofG:
        if feature == "intercept":
            PG = PG + PofG[feature]
        else:
            number = line.split("\t")[featuremap[feature]]
            if number == ".":
                number = "0"
            PG = PG + PofG[feature]*float(number)
    for feature in PofS:
        if feature == "intercept":
            PS = PS + PofS[feature]
        else:
            number = line.split("\t")[featuremap[feature]]
            if number == ".":
                number = "0"
            PS = PS + PofS[feature]*float(number)
    PJ = max(0,min(1,PJ))
    PG = max(0,min(1,PG))
    PS = max(0,min(1,PS))
    outfile.write(line.replace("eol\n","") + str(PJ) + "\t" + str(PG) + "\t" + str(PS) + "\teol\n")

datafile.close()
outfile.close()
