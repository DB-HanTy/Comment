-- 1.�����б�
-- 1.1 �Ż�ȯid
local voucherId = ARGV[1]
-- 1.2 �û�id
local userId = ARGV[2]

-- 2.����key
-- 2.1 ���key
local stockKey = "seckill:stock:" .. voucherId
-- 2.2 ����key
local orderKey = "seckill:order:" .. userId

-- 3. �ű�ҵ��
-- 3.1 �жϿ���Ƿ���� get stockKey
if (tonumber(redis.call("get", stockKey)) <= 0) then
    -- 3.2 ��治�㣬����1
    return 1
end
-- 3.2.�ж��û��Ƿ��µ� sismember orderKey userId
if (redis.call("sismember", orderKey, userId) == 1) then
    -- 3.3 �û��Ѿ��µ�������2
    return 2
end
-- 3.4 �ۼ���棬incrby stockKey -1
redis.call("incrby", stockKey, -1)
-- 3.5 ����������sadd orderKey userId
redis.call("sadd", orderKey, userId)
return 0
