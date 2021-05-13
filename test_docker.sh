#!/bin/bash

docker exec -it ktor-run curl --request POST -H "Content-Type:application/json" -d "{\"name\":\"mella\",\"password\":\"abc123\",\"email\":\"mel@a.a\",\"roleId\":1,\"balance\":1700}" "http://127.1.0.1:8080/auth/signup"
if docker exec -it mysql1 mysql -uadmin -h127.1.0.1 -pabc123 mytestdb -e"select name from users where name = 'mella' and role_id = 1" | grep 'mella'
  then echo "test 1 ok"
  else
    echo "test 1 not ok"
    exit 1
fi

docker exec -it ktor-run curl --request POST -H "Content-Type:application/json" -d "{\"name\":\"mas\",\"password\":\"def456\",\"email\":\"m@a.a\",\"roleId\":2,\"balance\":65700}" "http://127.1.0.1:8080/auth/signup"
if docker exec -it mysql1 mysql -uadmin -h127.1.0.1 -pabc123 mytestdb -e"select name from users where name = 'mas' and role_id = 2" | grep 'mas'
  then echo "test 2 ok"
  else
    echo "test 2 not ok"
    exit 1
fi

docker exec -it ktor-run curl --request GET -H "Content-Type:application/json"  "http://127.1.0.1:8080/auth/login?email=mel%40a.a&password=abc123"
if docker exec -it mysql1 mysql -uadmin -h127.1.0.1 -pabc123 mytestdb -e"select user_id from sessions where user_id = 1" | grep 'user_id'
  then echo "test 3 ok"
  else
    echo "test 3 not ok"
    exit 1
fi

docker exec -it ktor-run curl --request GET -H "Content-Type:application/json"  "http://127.1.0.1:8080/auth/login?email=m%40a.a&password=def456"
if docker exec -it mysql1 mysql -uadmin -h127.1.0.1 -pabc123 mytestdb -e"select user_id from sessions where user_id = 2" | grep 'user_id'
  then echo "test 4 ok"
  else
    echo "test 4 not ok"
    exit 1
fi


docker exec -it ktor-run curl --request POST -H "Authorization:<seller1_token>" -d "[{\"id\":1,\"name\":\"Buku\",\"price\":1500,\"owner\":1},{\"id\":2,\"name\":\"Nasi\",\"price\":2500,\"owner\":1},{\"id\":3,\"name\":\"Bata\",\"price\":2700,\"owner\":1}]" "http://127.1.0.1:8080/items/"
if docker exec -it mysql1 mysql -uadmin -h127.1.0.1 -pabc123 mytestdb -e"select name from items where name = 'Nasi' and owner = 1" | grep 'Nasi'
  then echo "test 5 ok"
  else
    echo "test 5 not ok"
    exit 1
fi

docker exec -it ktor-run curl --request POST -H "Authorization:<token>" -d "{\"stock\":10}" "http://127.1.0.1:8080/items/stock/3"
if docker exec -it mysql1 mysql -uadmin -h127.1.0.1 -pabc123 mytestdb -e"select * from item_stocks where item_id = 3 and count = 10" | grep 'count'
  then echo "test 6 ok"
  else
    echo "test 6 not ok"
    exit 1
fi

docker exec -it ktor-run curl --request POST -H "Authorization:<buyer_token>" -d "{\"itemId\":3,\"count\":5}" "http://127.1.0.1:8080/trans/order"
if docker exec -it mysql1 mysql -uadmin -h127.1.0.1 -pabc123 mytestdb -e"select * from transactions where buyer = 2 and seller = 1 and item_id = 3 and count = 5 and status = 1" | grep 'buyer'
  then echo "test 7 ok"
  else
    echo "test 7 not ok"
    exit 1
fi

docker exec -it ktor-run curl --request POST -H "Authorization:<seller1_token>"  "http://127.1.0.1:8080/trans/approve/1"
if docker exec -it mysql1 mysql -uadmin -h127.1.0.1 -pabc123 mytestdb -e"select * from transactions where id = 1 and buyer = 2 and seller = 1 and status = 2" | grep 'seller'
  then echo "test 8 ok"
  else
    echo "test 8 not ok"
    exit 1
fi


docker exec -it ktor-run curl --request POST -H "Authorization:<buyer_token>"  "http://127.1.0.1:8080/trans/pay/1"
if docker exec -it mysql1 mysql -uadmin -h127.1.0.1 -pabc123 mytestdb -e"select * from transactions where id = 1 and buyer = 2 and seller = 1 and status = 4" | grep 'buyer'
  then echo "test 9 ok"
  else
    echo "test 9 not ok"
    exit 1
fi

if docker exec -it mysql1 mysql -uadmin -h127.1.0.1 -pabc123 mytestdb -e"select balance from users where id = 1 and balance = 26000" | grep '26000'
  then echo "test 10 ok"
  else
    echo "test 10 not ok"
    exit 1
fi

if docker exec -it mysql1 mysql -uadmin -h127.1.0.1 -pabc123 mytestdb -e"select balance from users where id = 2 and balance = 41400" | grep '41400'
  then echo "test 11 ok"
  else
    echo "test 11 not ok"
    exit 1
fi
