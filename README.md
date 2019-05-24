# PhotoSearch

## Introduction

**PhotoSearch** is a project that was mainly designed for personal skill presentation, improvement and... for fun!

Keyword search over image links and descriptions is its basic functionality.
It's also provide Named Entity Identification And Linking to DBpedia through DBpedia Spotlight API.
Moreover, the entities that are identified on the fly are also used for Information Extraction over DBpedia live open linked dataset through SPARQL Queries (Apache Jena is used as query API). Finally, the extracted information (i.e. types, categories and abstracts) are used for query expansion, which is useful mostly in cases where no images are able to be retrieved through the simple keyword functionality (i.e. Jaccard Similarity).

The aforementioned functionality is provided as GET Requests through a REST API, for which the JavaSpark Framework alongside with the embedded Jetty server is used.

### Note 1 
**Googles data-set for automatic image conceptual captioning (https://ai.googleblog.com/2018/09/conceptual-captions-new-dataset-and.html or https://ai.google.com/research/ConceptualCaptions) is used for the project and all rights reserved from Google Inc. There is no intention of hijacking or take advantage over the data-set since the project is only used for non-commercial purposes.**

### Note 2
**Use the path to the aforementioned data-set as input to the project for initializing the server properly. Any other data-set with the same format could be used as well.**
