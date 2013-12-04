select year,mes,sum(SUBTOTAL) as total
,sum(case when mes=1 then subtotal else 0 end) as ene
,sum(case when mes=2 then subtotal else 0 end) as feb
,sum(case when mes=3 then subtotal else 0 end) as mar
,sum(case when mes=4 then subtotal else 0 end) as abr
,sum(case when mes=5 then subtotal else 0 end) as may
,sum(case when mes=6 then subtotal else 0 end) as jun
,sum(case when mes=7 then subtotal else 0 end) as jul
,sum(case when mes=8 then subtotal else 0 end) as ago
,sum(case when mes=9 then subtotal else 0 end) as sep
,sum(case when mes=10 then subtotal else 0 end) as oct
,sum(case when mes=11 then subtotal else 0 end) as nov
,sum(case when mes=12 then subtotal else 0 end) as dic
 from SW_VENTAS where year=? @ORIGEN 
group by mes,year