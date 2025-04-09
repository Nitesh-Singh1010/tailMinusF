## Implement a simple Java web service that allows one to interact with two web-browsers, f viz., Google Chrome and Mozilla Firefox. 



First browser can start other browser with a URL, stop it, cleanup cache, history, etc. and get the current active tab (assume one window). RESTful web service was to be implemented.


    * Start: http://<server>/start?browser=chrome&url=http://exa mple.com should start Google Chrome and open http://example.com in the same.

    * Stop: http://<server>/stop?browser=<browser> should stop the given browser if it is running.

    * Get Active Tab:  http://<server>/geturl?browser=<browser> should get the current active tab URL for the given browser.

    * Cleanup: http://<server>/cleanup?browser=<browser should clean up the browsing session for the given browser if it has been stopped.