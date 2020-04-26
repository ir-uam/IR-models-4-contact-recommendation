#
# Implementation of pairwise Tukey test
#
# @author Javier Sanz-Cruzado (javier.sanz-cruzado@uam.es)
# @author Pablo Castells (pablo.castells@uam.es)
# @author Craig Macdonald (craig.macdonald@glasgow.ac.uk)
# @author Iadh Ounis (iadh.ounis@glasgow.ac.uk)
pairwiseTukeyTest <- function (data)
{
    systemnames <- colnames(data)
    
    n <- nrow(data)
    m <- ncol(data)
    mn <- m*n
      
    datamatrix <- as.matrix(data)
    systemmeans <- colMeans(data)
    usermeans <- rowMeans(data)
    grandmean <- mean(systemmeans)
    
    # Then, we compute SE2
    se2 <- 0
    auxlist <- rep(0,m)
    for(i in 1:m)
    {
      smean <- systemmeans[i]
      auxval <- 0
      for(j in 1:n)
      {
        aux <- datamatrix[j,i] - smean - usermeans[j] + grandmean
        auxval <- auxval + aux*aux
      }
      auxlist[i] <- auxval
    }
    
    se2 <- sum(auxlist)
    ve2 <- se2/((n-1)*(m-1))
    
    pvalues <- matrix(0, m, m)
    colnames(pvalues) <- systemnames
    rownames(pvalues) <- systemnames
    
    # Now, we compute the statistical test for each pair of users:
    for (i in 2:m)
    {
      for (j in 1:(i-1))
      {
        a <- as.numeric(systemmeans[i])
        b <- as.numeric(systemmeans[j])
        statistic <- as.numeric(abs(a-b)/sqrt(ve2/n))
        ptukeyval <- ptukey(statistic, m, (n-1)*(m-1), lower.tail = FALSE)
        pvalues[i,j] <- ptukeyval
        pvalues[j,i] <- ptukeyval
      }
    }
    
    pvalues
}
