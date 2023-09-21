update keycloak.user_entity
set username = regexp_replace(username, '[^a-z0-9]', '_', 'g');
update keycloak.user_attribute
set value = regexp_replace(value, '[^a-z0-9]', '_', 'g')
where name = 'username';
