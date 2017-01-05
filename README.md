# Dribbbee
Dribbble Android Client

#### <i class="icon-login"></i> 1. Login with user's Dribbble account
> **Note:** using **[OkHttp](http://square.github.io/okhttp/)** and **[WebView](https://developer.android.com/reference/android/webkit/WebView.html)** to let user go through [OAuth](http://developer.dribbble.com/v1/oauth/) process of Dribbble

#### <i class="icon-file"></i> 2. Using NavigationDrawer and Toolbar to organise Main UI
#### <i class="icon-download"></i> 3. Load data by Dribbble Restful API with **AsyncTask**
#### <i class="icon-picture"></i> 4. Loading and caching images using third party library
>● [Picasso](http://square.github.io/picasso/) (no gif support)
>
>● [Fresco](http://frescolib.org/)

#### <i class="icon-refresh"></i> 5. Implement **Infinite loading list** with mixed-typed adapter and AsyncTask. 


<br>
<br>
<br>
<br>
----------

<br>



#### Diagram of Activities, Fragments with Dribbble API for Dribbble shot/like/bucket features
<br>

![enter image description here](https://lh3.googleusercontent.com/-jBvRElaWANY/WGzOW5TB46I/AAAAAAAAAAU/BiOdCBQ7POghrbw_IkEz9OHZAOEYtmHlACLcB/s0/Screen+Shot+2017-01-04+at+6.28.00+pm.png "oauth.png")

![enter image description here](https://lh3.googleusercontent.com/-KGENNN4Yi6s/WG2zzn4Vt3I/AAAAAAAAABg/d2D1gUg3QAw0VYX5936DeVdYTzw5qmysgCLcB/s0/Screen+Shot+2017-01-05+at+10.44.26+am.png "org.png")

![enter image description here](https://lh3.googleusercontent.com/-nYZgCT3FUmU/WG2z5dDh7xI/AAAAAAAAABo/qX8YMdJTm-UjI2TQTcKAHnBoLu2mXCf1QCLcB/s0/Screen+Shot+2017-01-05+at+10.45.01+am.png "like-bucket.png")


|              | Dribbble REST API                                                                   | Description                                               |
|---------------|-------------------------------------------------------------------------------------|-----------------------------------------------------------|
| shots         | GET /shots                                                                          | Get the shots                                             |
| shots         | GET /shots/:id/like                                                                 | Check if user liked a shot                                |
| shots         | POST /shots/:id/like <br> DELETE /shots/:id/like                                    | Like / Unlike a shot                                      |
| user          | GET /user/likes                                                                     | Get the shots userliked                                   |
| user          | GET /user/buckets                                                                   | Get the shots user bucketed                               |
| user, buckets | ![enter image description here](https://lh3.googleusercontent.com/-y0kSX7uVOEo/WG24Qjey-bI/AAAAAAAAAB8/Go3_OR-K9YMSptYaFFC0AWJGzo-CfWQxQCLcB/s0/Screen+Shot+2017-01-05+at+10.56.48+am.png "bucket.png")                                                                                    | Check if user bucketed a shot                             |
| buckets       | PUT /buckets/:id/shots <br> body={shot_id:**} <br> <br> DELETE /buckets/:id/shots   | Add a shot into a bucket <br> Remove a shot from a bucket |


