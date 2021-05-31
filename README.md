# Task
>  Zadanie polega na napisaniu programu dostarczającego dane uczące do systemu uczenia maszynowego. System ten służy do analizy (tagowania) zawartości filmów, a Twoim zdaniem jest zasilić go danymi. Wejściem do programu, który musisz napisać, jest plik zawierający identyfikatory filmów na YouTube. Identyfikatorem filmu na YT jest zaznaczony fragment adresu: https://www.youtube.com/watch?v=6Af6b_wyiwI. Dla każdego filmu związanego z identyfikatorem musisz ściągnąć napisy w języku angielskim. W następnym kroku, z napisów wyekstrahuj nazwy własne i/lub rzeczowniki (zaproponuj dowolną heurystykę), a następnie dla tych nazw własnych/rzeczowników przypisz treść artykułów z Wikipedii (angielskiej) wraz z linkami do nich. Wyjściem programu ma być plik z listą “szóstek”: (identyfikator z YT, napisy w formie surowej, napisy w formie płaskiego (plain) tekstu, treść artykułu w formie surowej, treść artykułu w formie płaskiego (plain) tekstu, link do artykułu). Zaproponuj jakiś format czytelny dla człowieka i łatwo przetwarzalny dla maszyny. Krótko opisz swoje rozwiązanie i zaimplementowane pomysły w języku angielskim w pliku README.

# My solution

To solve the problem I've used a Akka Http Client and Stanford NLP.
## YouTube API
I create a HttpRequest to YouTube using timedtext API. I've been trying to use a Java YouTube API, but after tests I'm got a problem with Subtitle API. An API only works when video is public to edit subtitles or if video is in my account, so I've used timedtext API. I'm geeting YT links from file and filter by regex to extract only YT links. Before send a request im putting an video ids to set to prevent duplicates. My function return a set of YTResponse classes.
## Stanford NLP
Every YTResponse class has plain texted subtitles which are passed to my NLPFilter object. I was created an NounFilter trait because someone who dont want my filter can write own one. Filter returns a set of nouns. It will be saved to YTReady class.
## Wikipedia REST API
Set of YTReady class are mapped to single YTReady class and later are mapped to single noun request. Every response is saved to WikipediaArticles class. When it  finishes, list with WikipediaArticles is created and attached to YouTubeVideo class to serialize the result.
The result of function is list of YouTubeVideo objects.

I used Circe to convert result into JSON format.

## To use a program you need to assign >=5GB of RAM to JVM

