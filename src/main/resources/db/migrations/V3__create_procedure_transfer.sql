create procedure transfer (
   source int,
   target int,
   amount decimal
)
language plpgsql
as $$
begin
    update accounts set balance = balance - amount where id = source;
    update accounts set balance = balance + amount where id = target;
    commit;
end;$$
