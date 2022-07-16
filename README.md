# SearchAds

## Abstract
This is a project which implements a SearchAds Engine for online shopping websites, with the following features:

 <ol>
 <li>Web crawler to crawl product data from online shopping website (Java, JSoup, RabbitMQ)</li>
 <li>Search Ads web service with the following workflow: </li>
   <ol>
     <li>Data cleaning</li>
     <li>Query understanding</li>
     <li>Query rewrite</li>
     <li>Ads selection from inverted index</li>
     <li>Ads ranking</li>
     <li>Ads filter</li>
     <li>Ads pricing</li>
     <li>Ads allocation</li>
     
   </ol>
 <li>Distributed Ads Index Server using gRPC to send Ads candidate to Ads Web Server</li>
 <li>Applied Word2Vec and TF-IDF algorithms to generate synonyms for Query rewrite and calculate Ads relevance score for Ads ranking (Python, SparkMLlib)</li>
 <li>Predict click probability with features generated from simulated search log (Python, SparkMLlib)</li>
 </ol>
 
## Tech stacks used
<ul>
  Programming languages:
  <ul>
    <li>Java</li>
    <li>Python</li>
  </ul>

  Databases:
  <ul>
    <li>MySQL</li>
    <li>MemCached</li>
  </ul>

  Web development:
  <ul>
    <li>Jetty</li>
    <li>gRPC</li>
  </ul>

  ML tools:
  <ul>
    <li>SparkMLlib</li>
  </ul>
  
  ML algorithms and models:
  <ul>
    <li>Word2Vec</li>
    <li>TF-IDF</li>
    <li>Logistic regression</li>
  </ul>
</ul>
