CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    role VARCHAR(50) NOT NULL DEFAULT 'ROLE_USER',
	profile_image_url VARCHAR(500),
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME DEFAULT NULL
);

CREATE TABLE chat_rooms (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    is_group BOOLEAN DEFAULT FALSE,
    room_owner_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME DEFAULT NULL
);

CREATE TABLE chat_room_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    chat_room_id BIGINT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME DEFAULT NULL
);


CREATE TABLE messages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    chat_room_id BIGINT NOT NULL,
    sender_id BIGINT NOT NULL,
    content TEXT NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME DEFAULT NULL
);

CREATE TABLE user_profile (
    user_id BIGINT PRIMARY KEY,
    profile_image_url VARCHAR(500)
);



 create table chat_room_last_message (
	chat_room_id BIGINT NOT NULL,
    message_id BIGINT NOT NULL,
    shard_key INT NOT NULL,
	created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    modified_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    deleted_at DATETIME DEFAULT NULL,
	PRIMARY KEY (chat_room_id)
    );



show databases;
use minimain;
use minimessage0;
use minimessage1;

select * from minimain.users;
select * from minimain.chat_rooms;
select * from minimain.chat_room_members;

select * from minimessage0.messages;
select * from minimessage1.messages;

-- delete from minimain.chat_rooms where id = 1;
-- delete from minimessage0.messages where id = 3;
-- drop table minimain.chat_rooms;
-- drop table minimessage1.chat_rooms;
















insert into users(username, email) values ('gooha', 'goo@naver.com'), ('joon', 'joon@naver.com'), ('hee', 'hee@naver.com');
insert into chat_rooms(name, is_group) values ('room1', true);
insert into chat_rooms(name) values ('room2');
insert into chat_room_members(user_id, chat_room_id) values(1,1), (2,1);
insert into chat_room_members(user_id, chat_room_id) values(1,2), (3,2);

insert into messages(id, chat_room_id, sender_id, content) values (1, 1, 1, '안녕하세요 전 goo에요');
insert into messages(id, chat_room_id, sender_id, content) values (2, 1, 2, '안녕하세요 전 joon이에요');

insert into messages(id, chat_room_id, sender_id, content) values (1, 2, 1, '안녕? hee 난 goo야');
insert into messages(id, chat_room_id, sender_id, content) values (2, 2, 3, '하이 goo 난 hee');