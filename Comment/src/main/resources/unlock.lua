-- �Ƚ��̱߳�ʶ�����еı�ʾ�Ƿ�һ��
if (redis.call('get',KEYS[1] == ARGV[1])) then
    --�ͷ���
    return redis.call('del',KEYS[1])
end
return 0
