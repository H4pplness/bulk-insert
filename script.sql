-- Tiền xử lý 
/*
Gọi Api {URI}/engineer/insert-engineers để thực hiện lấy 1 triệu bản ghi (1 lần gọi
là 1 triệu bản ghi được sinh random vào bảng engineer_sync) 
*/


-- 1. Tạo table
CREATE TABLE engineer(
	id integer primary key,
	first_name varchar(256),
	last_name varchar(256),
	gender smallint,
	country_id smallint,
	title varchar(100),
	started_date date
)

VACUUM FULL

CREATE TABLE engineer_sync(
	id integer primary key,
	first_name varchar(256),
	last_name varchar(256),
	gender smallint,
	country_id smallint,
	title varchar(100),
	started_date date,
	sync_status smallint
)

-- 2. So sánh việc tận dụng index và không sử dụng
-- Seq scan (không sử dụng index, duyệt tuần tự)
EXPLAIN ANALYZE 
SELECT e.* FROM engineer_sync e
WHERE e.id%1000=1
LIMIT 1000

-- Tận dụng index mặc định của id (postgres cũng như oracle đều có index mặc định cho id)
EXPLAIN ANALYZE 
SELECT * FROM engineer_sync 
WHERE id BETWEEN 100000 AND 101000
ORDER BY id
LIMIT 1000


-- 3. Phân mảnh dữ liệu 
-- Seq scan sẽ phải quét qua cả những bản ghi đã bị đánh dấu là không sử dụng 
EXPLAIN ANALYZE 
SELECT ctid,e.* FROM engineer_sync e
WHERE e.id%1000=1 AND sync_status=1
LIMIT 1000;

-- Sử dụng index sẽ giảm được số bản ghi bị đánh dấu là không sử dụng 

/* 
Thực chất index vẫn tồn tại những bản ghi bị đánh dấu là bỏ đi nhưng 
nếu xét về index của id thì hầu như không có sự thay đổi nên gần như 
sẽ không tồn tại bản ghi bị đánh dấu là bỏ đi trong các page chứa index.
*/
EXPLAIN ANALYZE 
SELECT ctid,e.* FROM engineer_sync e
WHERE id BETWEEN 1 AND 1000

-- 4. Sử dụng VACUUM để dọn dẹp dữ liệu bị bỏ đi -> Tăng tốc 20~30% tốc độ scan và update. 

VACUUM FULL engineer;
VACUUM FULL engineer_sync;

-- 5. Tại sao không thực hiện partition cho sync_status
/*
Việc partition cho sync_status mặc dù việc partition cho sync_status có thể cải
thiện đáng kể cho tốc độ lấy ra các bản ghi có sync_status = 0 để thực hiện đồng bộ.
Tuy nhiên nó lại có một nhược điểm đó là mỗi lần update trạng thái của sync_status 
thì sẽ phải update sync_status lại, và khi update sync_status lại thì bản ghi sẽ được 
chuyển qua bên partition còn lại dẫn đến tác động một vài phần cấu trúc của cả 2 partition 
chẳng hạn như index của cả 2 partition sẽ phải cập nhật lại.
*/