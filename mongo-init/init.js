db = db.getSiblingDB("admin");

db.createUser({
    user: "mongoUser",
    pwd: "mongoPass",
    roles: [{role: "readWrite", db: "retail"}]
});

