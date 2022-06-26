Remember to kill all the process after runtime like this:

```sudo kill -9 $(sudo lsof -t -i:8001)```