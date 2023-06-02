<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Insert title here</title>
<link rel="stylesheet" href="/mh/css/member/delete_account.css">
</head>
<body>
	<div class="membership_withdrawal">
		<div class="membership_withdrawal_title">회원 탈퇴</div>
		<div class="membership_withdrawal_box">
			<form>
				<div class="withdrawal_check_box_title">
					metahouse를 떠나는 이유를 알려주세요.
				</div>
				<ul class="withdrawal_check_box">
					<li><input type="checkbox"> 이용하고 싶은 서비스가 없어요</li>
					<li><input type="checkbox"> 서비스 퀄리티가 낮아요</li>
					<li><input type="checkbox"> 비매너 회원을 만났어요</li>
					<li><input type="checkbox"> 잦은 오류가 발생해요</li>
					<li><input type="checkbox"> 더 나은 서비스를 찾았어요</li>
					<li><input type="checkbox"> 기타</li>
				</ul>
				<div class="withdrawal_message">
					<div class="withdrawal_message_detail_title">
						탈퇴시 주의사항
					</div>
					<ul class="withdrawal_message_detail">
						<li>계정 탈퇴 시, 모든 서비스에서 탈퇴가 진행됩니다.</li>
						<li>탈퇴 시 계정과 관련된 모든 권한이 사라지며 복구가 불가능합니다.</li>
						<li>직접 작성한 콘텐츠는 자동으로 삭제됩니다. 만약 직접 삭제를 원하시면 탈퇴 이전에 삭제 해주세요.</li>
						<li>탈퇴 후 동일한 메일로 재가입 가능합니다. 하지만 탈퇴한 계정과 연동은 불가능합니다.</li>
						<li>탈퇴 후 연동된 소셜 계정 정보도 사라지며 소셜 로그인으로 기존 계정 이용 불가능합니다.</li>
						<li>현재 비밀번호를 입력하고 탈퇴하기를 누르시면 위 내용에 동의하는 것으로 간주됩니다.</li>
						<li class="withdrawal_agree_check"><input type="checkbox"> 주의사항을 모두 읽고 동의합니다.</li>
					</ul>
				</div>
				<div class="withdrawal_area">
					<input type="password" placeholder="비밀번호를 입력하세요." class="withdrawal_password_check">
					<input type="button" value="탈퇴하기" class="withdrawal_button">
				</div>
			</form>
		</div>
	</div>
</body>
</html>