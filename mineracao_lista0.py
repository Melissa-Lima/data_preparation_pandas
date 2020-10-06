import seaborn as sns, numpy as np
import matplotlib.pyplot as Plt
import math
from random import randrange

# mi = 3 e 8, sigma^2 = 1 e 4
def distr_gauss_fx(x):
    fx = ((1/(math.sqrt(2*math.pi*1**2))) * math.exp((-1/2)*(((x-3)/1)**2))) + ((1/(math.sqrt(2*math.pi*2**2))) * math.exp((-1/2)*(((x-math.sqrt(8))/2)**2)))
    return fx

# mi = 6, sigma^2 = 10
def distr_gauss_gx(x):
    gx = (1/(math.sqrt(2*math.pi*math.sqrt(10)**2))) * math.exp((-1/2)*(((x-math.sqrt(6))/math.sqrt(10))**2))
    return gx

# criando as 1000 amostras e o M
aceitos = []
amostras = np.random.normal(6,math.sqrt(10),1000)
M = 0
for x in range(len(amostras)):
    a = distr_gauss_fx(amostras[x])/distr_gauss_gx(amostras[x])
    if(a > M):
        M = a

# selecionando amostras que satisfazem a condição e atualizando o M
for i in range(1000000) :
    u = randrange(0,1)
    z = np.random.normal(6,math.sqrt(10),1)
    if (u < (distr_gauss_fx(z)/(M*(distr_gauss_gx(z))))):
        aceitos.append(z)
        M = max(M,(distr_gauss_fx(z)/distr_gauss_gx(z)))

sns.distplot(aceitos)