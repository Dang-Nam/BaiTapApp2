# BaiTapApp2
# Đề bài: viết app nhận thông báo
# Sinh viên: Đặng Phương Nam


Yêu cầu


+ Viết app android, sử dụng timer để 30s/lần lấy dữ liệu từ
+ Lấy được last_id thì tải dữ liệu về (nếu mới hơn last_id đã lưu lần trước). Hiển thị thông báo, âm thanh gây chú ý!


Bài làm

+ thêm thư viện Volley vào dự án Android
![image](https://github.com/user-attachments/assets/0d1136fa-064a-4d75-9d5c-c79731846371)

+ tạo giao diện đơn giản với 1 textview dùng để hiển thị last_id mới nhất từ API. 1 button "Kiểm tra dữ liệu" để lấy dữ liệu mới từ API.

![image](https://github.com/user-attachments/assets/d2772a37-e1e9-4b6e-bd54-f2244d143d58)

+ thêm file âm thanh thông báo vào thư mục res/raw

![image](https://github.com/user-attachments/assets/dca94023-2df3-4851-a091-4bd5b264172a)

+ Kiểm tra dữ liệu sau mỗi 30s
![image](https://github.com/user-attachments/assets/666f806b-b195-4b69-b38d-eeb7fe7116ee)

+ Lấy last_id và so sánh với lastSavedId
![image](https://github.com/user-attachments/assets/5ea0ffa6-b17f-4d9d-8ec2-7f310a49182d)

+ Hiển thị thông báo và phát âm thanh khi có dữ liệu mới
![image](https://github.com/user-attachments/assets/bdf6fbe3-c3ad-4b72-9b76-3963f64e56c1)
![image](https://github.com/user-attachments/assets/7ed13354-b1d1-4fa1-adb4-b533a066f3e6)

+ kết quả chạy app
![image](https://github.com/user-attachments/assets/7cfc908d-c8a2-497f-a2d8-a51f774b5635)
![image](https://github.com/user-attachments/assets/2d4122b7-ed2c-4b3b-a736-afe67b824ca6)



