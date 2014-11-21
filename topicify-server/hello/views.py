from django.http import HttpResponse
from django.core.exceptions import SuspiciousOperation
from gensim import corpora, models, similarities
from gensim.models import hdpmodel, ldamodel

NUMTOPICS = 10
CORPUSNAME = 'corp.txt'

# Create your views here.
# def index(request):
#     return HttpResponse('Hello from Python!')

def lda(request):
    if request.method != 'POST':
        raise SuspiciousOperation

    numTopics = NUMTOPICS
    corpusfile = open(CORPUSNAME, 'a+')
    corpusfile.write(request.read())
    corpusfile.seek(0)
    speechLines = corpusfile.readlines()
    corpusfile.close()

    # remove common words and tokenize
    stoplist = set('not from with let its by than some at on all do are have but is will i be our we that as this it for a of the and to in'.split())
    texts = [[word for word in document.lower().split() if word not in stoplist] for document in speechLines]

    # generate dictionary
    topicdict = corpora.Dictionary(texts)

    # remove words with low freq.  3 is an arbitrary number I have picked here
    low_occurance_ids = [tokenid for tokenid, docfreq in topicdict.dfs.iteritems() if docfreq == 3]
    topicdict.filter_tokens(low_occurance_ids)
    topicdict.compactify()
    corpus = [topicdict.doc2bow(t) for t in texts]
    
    # Generate LDA Model
    lda = models.ldamodel.LdaModel(corpus, num_topics=numTopics)
    i = 0
    response = HttpResponse()
    
    # We print the topics
    for topic in lda.show_topics(num_topics=numTopics, formatted=False, num_words=10):
        i = i + 1
        response.write('Topic #%d:\n' % i)
        for p, id in topic:
            response.write(topicdict[int(id)] + '\n')
            
        response.write('\n')

    response['num_topics'] = i
    return response

