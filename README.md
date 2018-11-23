# CITlabTextAlignment
A tool that aligns text/ground truth to a given list of ConfMats

[![Build Status](http://dbis-halvar.uibk.ac.at/jenkins/buildStatus/icon?job=CITlabTextAlignment)](http://dbis-halvar.uibk.ac.at/jenkins/job/CITlabTextAlignment)

## Requirements
- Java >= version 8
- Maven
- All further dependencies are gathered via Maven

## Build
```
git clone https://github.com/CITlabRostock/CITlabTextAlignment
cd CITlabTextAlignment
mvn package [-DskipTests=true]
```
## Usage:
The most important class is ``de.uros.citlab.textalignment.TextAligner``.
#### Method
The method
```
public List<LineMatch> getAlignmentResult(
    List<String> refs,
    List<ConfMat> recos
    )
```
tries to align the references to the given ConfMats.
The Result ``List<LineMatch> result`` is a list from the same size and order as the ConfMats.
When ``result.get(i) != null``, for the ConfMat ``recos.get(i)`` a corresponding transcript is available.
The reference and confidence of ConfMat ``i``  is available by using
```
LineMatch match = result.get(i);
String reference = match.getReference(); 
double confidence = match.getConfidence();
```
The confidence is in \[0.0,1.0\], whereas the higher the value,
the better is the alignment.
A confidence greater 0.1 can be seen as very trustful,
whereas confidences lower 0.01 are viewed with caution. 
#### Base Parameters
To configure the alignment tool, several parameters are available.
The most important parameters are required via the constructor:
```
public TextAligner(
    String lineBreakCharacters,
    Double costSkipWords,
    Double costSkipConfMat,
    Double costJumpConfMat
    )
```
requires a lot of parameters which will be breefly explained.
- __lineBreakCharacters__
The algorithm requires a String with characters which can be interpreted als line break.
In most cases, the space character ``\u+0020`` is used,
but also the character tabulation ``\u+0009`` could make sense.
In case that boch characters should be used set
``String lineBreakCharacters = "\u0009\u0020"``.
Note that setting ``String lineBreakCharacters = ""`` is also possible,
if only whole lines of the reference should be mapped to the ConfMats.
It is __not allowed__ to set the line feed ``\u+000A`` as line break character.
- __costSkipWords__
In some szenarios either there are transcripts available,
that do not occure in the ConfMats or ConfMats are missing.
Both cases require the need to skip transcripts
 when it is too hard to align them to the ConfMats.
 If ``Double costSkipWords = null``, the algorithm is not allowed to skip words.
 So he is forced to read each reference in the ConfMats.
 If ``Double costSkipWords = 0.0`` the algorithm will try to skip any word,
 if it is possible.
 A good value is ``Double costSkipWords = 4.0``.
 - __costSkipConfMat__
In some szenarios either there are ConfMats available,
that have no corresponding transcripts or transcripts for ConfMats are not given.
In both cases it should be possible not to force the algorithm to align text to a ConfMat.
If ``Double costSkipConfMat = null``, the algorithm is not allowed to skip a ConfMat,
so he is forced to align the ConfMat to any available transcript.
If ``Double costSkipConfMat = 0.0``, the algorithm will try to skip any ConfMat,
if it is possible.
A good value is  ``Double costSkipConfMat = 0.2``.
- __costJumpConfMat__
In some szenarios the reading order of the transcripts and ConfMats is not consistent.
In these cases it is necessary to ignore the given reading order.
If ``Double costJumpConfMat = null`` the algorithm is not allowed to change the reading order.
For ``Double costJumpConfMat = 0`` the algorithm can chose an arbitrary reading order.
Note that the complexity of the algorithm singnificantly increases when ``Double costJumpConfMat != null``
and the alignment result is only a heuristic.
Especially if the alignement task contains many short lines and ConfMats,
the algorithm can fail.
If a value ``> 0`` is used the algorithm is penalized if it breaks the original reading order of the ConfMats.
A good value is  ``Double costJumpConfMat = 6.0``.

#### Additional Parameters
- __threshold__
Instead of accepting only alignments with a specific threshold given by ``lineMatch.getConfidence()``,
the threshold can be set in advance by the method ``public void setThreshold(double threshold)``.
The default is ``threshold = 0.0``.
- __hyphenation__
In some cases the transcription contains text without hyphenations,
whereas they occur "in" the ConfMats. It is possible to define the hyphenation propery
```
public HyphenationProperty(
    boolean skipSuffix,
    boolean skipPrefix,
    char[] prefixes,
    char[] suffixes,
    double hypCosts
    )
```
In general one can specify characters that were used as hyphenation signs (see ``prefixes`` and ``suffixes``).
In addition it is possible to make their occurance optional (see ``skipSuffix`` and ``skipPrefix``).
To do not allow the algorithm to see hyphenations in too many places,
extra costs for a hyphenation can be added.
With ``hypCosts = 0`` there will be no extra costs, whereas ``hypCosts = Double.POSITIVE_INFINITY`` would permit any hyphenation.
A good value is ``hypCosts = 6.0``.
 __Examples:__
 
With the hyphenation property
```
new HyphenationProperty(false, false, null, new char[]{'-', '¬'}, 6.0)
```
 the ground truth ``hyphen``
can be interpretet as ``"h-" "yphen"``, ``"h¬" "yphen"``, ``"hy-" "phen"``, ``"hy¬" "phen"``, ... , ``"hyphe-" "n"`` or ``"hyphe¬" "n"``.
 
With the hyphenation property
```
new HyphenationProperty(false, true, new char[]{'='}, new char[]{'='}, 6.0)
```
the ground truth ``hyphen``
 can be interpretet as ``"h=" "yphen"``, ``"h=" "=yphen"``, ``"hy=" "phen"``, ``"hy=" "=phen"``, ... , ``"hyphe=" "n"`` or ``"hyphe=" "=n"``.   

In fact hyphenations are not allowed between all characters (like ``"h-" "yphen"``).
Therefore, a language patttern can be provided
so that hyphenations are restricted to language-specific properties.
So
```
new HyphenationProperty(false, false, null, new char[]{'-', '¬'}, 6.0, Hyphenator.HyphenationPattern.EN_US)
```
would only allow the hyphenations ``"hy-" "phen"`` and ``"hy¬" "phen"``.
Note that these ``HypenationPattern`` can __fail for special words__,
so that they have to be used with caution.

## LICENCE
see [LICENCE](LICENCE) and [NOTICE.md](https://github.com/CITlabRostock/CITlabLicensedBoM/blob/master/NOTICE.md)
