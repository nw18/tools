res_bad_request = "HTTP/1.1 500 Bad Requect\r\n\r\n"
res_not_found = "HTTP/1.1 404 Not Found\r\nContent-type: text/html\r\n\r\n";
res_ok = "HTTP/1.1 200 OK\r\nContent-type: text/html\r\n\r\n"
res_file_ok = "HTTP/1.1 200 OK\r\nContent-Length: {0}\r\nContent-type: {1}\r\n\r\n"
res_redirect = "HTTP/1.1 302 Temporarily Moved\r\nLocation: {0}\r\n\r\n"
res_text_ok = "HTTP/1.1 200 OK\r\nContent-type:text/plain\r\nContent-Length: {0}\r\n\r\n{1}"
res_text_ok_cookie = "HTTP/1.1 200 OK\r\nSet-Cookie: auid={2}; path=/;\r\nContent-type:text/plain\r\nContent-Length: {0}\r\n\r\n{1}"