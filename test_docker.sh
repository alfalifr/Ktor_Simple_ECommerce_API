#!/bin/bash

docker exec -it ktor-run curl --request POST -H "Content-Type:application/json" -d "{\"name\":\"mella\",\"password\":\"abc123\",\"email\":\"mel@a.a\",\"roleId\":1,\"balance\":1700}" "http://127.1.0.1:8080/auth/signup"
docker exec -it mysql1 mysql -uadmin -h127.1.0.1 -pabc123 mytestdb -e"select name from users where name = 'mella' and role_id = 1"

docker exec -it ktor-run curl --request POST -H "Content-Type:application/json" -d "{\"name\":\"mas\",\"password\":\"def456\",\"email\":\"m@a.a\",\"roleId\":2,\"balance\":65700}" "http://127.1.0.1:8080/auth/signup"
docker exec -it mysql1 mysql -uadmin -h127.1.0.1 -pabc123 mytestdb -e"select name from users where name = 'mas' and role_id = 2"

docker exec -it ktor-run curl --request GET -H "Content-Type:application/json"  "http://127.1.0.1:8080/auth/login?email=mel%40a.a&password=abc123"
docker exec -it mysql1 mysql -uadmin -h127.1.0.1 -pabc123 mytestdb -e"select user_id from sessions where user_id = 1"

docker exec -it ktor-run curl --request GET -H "Content-Type:application/json"  "http://127.1.0.1:8080/auth/login?email=m%40a.a&password=def456"
docker exec -it mysql1 mysql -uadmin -h127.1.0.1 -pabc123 mytestdb -e"select user_id from sessions where user_id = 2"


docker exec -it ktor-run curl --request POST -H "Authorization:<seller1_token>" -d "[{\"id\":1,\"name\":\"Buku\",\"price\":1500,\"owner\":1},{\"id\":2,\"name\":\"Nasi\",\"price\":2500,\"owner\":1},{\"id\":3,\"name\":\"Bata\",\"price\":2700,\"owner\":1}]" "http://127.1.0.1:8080/items/"
docker exec -it mysql1 mysql -uadmin -h127.1.0.1 -pabc123 mytestdb -e"select name from items where name = 'Nasi' and owner = 1"

docker exec -it ktor-run curl --request POST -H "Authorization:<token>" -d "{\"stock\":10}" "http://127.1.0.1:8080/items/stock/3"
docker exec -it mysql1 mysql -uadmin -h127.1.0.1 -pabc123 mytestdb -e"select * from item_stocks where item_id = 3 and count = 10"

docker exec -it ktor-run curl --request POST -H "Authorization:<buyer_token>" -d "{\"itemId\":3,\"count\":5}" "http://127.1.0.1:8080/trans/order"
docker exec -it mysql1 mysql -uadmin -h127.1.0.1 -pabc123 mytestdb -e"select * from transactions where buyer = 2 and seller = 1 and item_id = 3 and count = 5 and status = 1"

docker exec -it ktor-run curl --request POST -H "Authorization:<seller1_token>"  "http://127.1.0.1:8080/trans/approve/1"
docker exec -it mysql1 mysql -uadmin -h127.1.0.1 -pabc123 mytestdb -e"select * from transactions where id = 1 and buyer = 2 and seller = 1 and status = 2"


docker exec -it ktor-run curl --request POST -H "Authorization:<buyer_token>"  "http://127.1.0.1:8080/trans/pay/1"
docker exec -it mysql1 mysql -uadmin -h127.1.0.1 -pabc123 mytestdb -e"select * from transactions where id = 1 and buyer = 2 and seller = 1 and status = 4"
docker exec -it mysql1 mysql -uadmin -h127.1.0.1 -pabc123 mytestdb -e"select balance from users where id = 1 and balance = 26000"
docker exec -it mysql1 mysql -uadmin -h127.1.0.1 -pabc123 mytestdb -e"select balance from users where id = 2 and balance = 41400"
