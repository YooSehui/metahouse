package com.multi.metahouse.order.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.multi.metahouse.domain.dto.order.AssetOrdersDTO;
import com.multi.metahouse.domain.dto.order.ProjectOrdersDTO;
import com.multi.metahouse.domain.dto.order.SelectedAddOptionDTO;
import com.multi.metahouse.domain.entity.order.AssetOrdersEntity;
import com.multi.metahouse.domain.entity.order.ProjectOrdersDetailEntity;
import com.multi.metahouse.domain.entity.order.ProjectOrdersEntity;
import com.multi.metahouse.domain.entity.order.SelectedAddOptionEntity;
import com.multi.metahouse.domain.entity.order.dtoforjpa.AssetOrdersResponse;
import com.multi.metahouse.domain.entity.order.dtoforjpa.AssetOrdersResponse.Response;
import com.multi.metahouse.domain.entity.order.dtoforjpa.ProjectOrdersConfirmUpdateDTO;
import com.multi.metahouse.domain.entity.order.dtoforjpa.ProjectOrdersResponse;
import com.multi.metahouse.domain.entity.user.User;
import com.multi.metahouse.order.repository.dao.OrderDAO;
import com.multi.metahouse.order.repository.jpa.AssetOrdersRepository;
import com.multi.metahouse.order.repository.jpa.ProjectOrdersRepository;
import com.multi.metahouse.point.repository.dao.PointDAO;
import com.multi.metahouse.review.repository.dao.ReviewDAO;

@Service
public class OrderServiceImpl implements OrderService {
	OrderDAO dao;
	PointDAO pointDao;
	ReviewDAO reviewDao;
	ProjectOrdersRepository projectOrderRepository;
	AssetOrdersRepository assetOrderRepository;

	@Autowired
	public OrderServiceImpl(OrderDAO dao, PointDAO pointDao, ReviewDAO reviewDao,
			ProjectOrdersRepository projectOrderRepository, AssetOrdersRepository assetOrderRepository) {
		super();
		this.dao = dao;
		this.pointDao = pointDao;
		this.reviewDao = reviewDao;
		this.projectOrderRepository = projectOrderRepository;
		this.assetOrderRepository = assetOrderRepository;
	}

	@Override
	@Transactional
	public int orderA(AssetOrdersDTO assetOrder, User loginUser, int consumeAmount) {
		int payment = loginUser.getPoint() - consumeAmount;
		int result = 0;
		if (payment >= 0) {
			// 에셋 주문내역 생성
			dao.insertOrderA(assetOrder);
			// 포인트 결제 내역생성
			String consumeInfo = "Asset";
			consumeInfo = consumeInfo.concat(assetOrder.getAsset_id());
			pointDao.createConsumedPointInfo(loginUser, consumeAmount, consumeInfo);
			pointDao.consumePoint(loginUser, consumeAmount);

			result = 1;
		}
		return result;
	}

	@Override
	@Transactional
	public int orderP(ProjectOrdersDTO projectOrder, List<SelectedAddOptionDTO> options, User loginUser,
			int consumeAmount) {
		int payment = loginUser.getPoint() - consumeAmount;
		int result = 0;
		if (payment >= 0) {
			// 주문내역 생성
			dao.insertOrderP(projectOrder);
			// 옵션내역 생성
			for (int i = 0; i < options.size(); i++) {
				options.get(i).setOrder_id(projectOrder.getOrder_id());
				if (options.get(i).getAdd_option_id() != null) {
					dao.insertOrderOption(options.get(i));
				}
			}

			// 포인트 결제내역 생성
			String consumeInfo = "PJT";
			consumeInfo = consumeInfo.concat(projectOrder.getProject_id());
			pointDao.createConsumedPointInfo(loginUser, consumeAmount, consumeInfo);
			pointDao.consumePoint(loginUser, consumeAmount);

			result = 1;
		}

		return result;
	}

	/*---------------------- 민우 영역 ----------------------------- */
///////////////프로젝트 주문////////////////////////////
	private String getTotalPrice(ProjectOrdersEntity entity) {
		int optionPriceSum = 0;
		if (entity.getProjectId().getAddOptionEntityList() != null) {
			for (SelectedAddOptionEntity selected : entity.getSelectedOptionList()) {
				int optionPrice = selected.getAddOptionId().getAddOptionPrice();
				int optionCount = Integer.parseInt(selected.getCount());
				optionPriceSum += optionPrice * optionCount;
			}
		}
		int totalPrice = optionPriceSum + entity.getOrderPrice();
		return String.format("%,d", totalPrice);

	}

	private String getPackageType(ProjectOrdersEntity entity) {
		Object packageEntity = null;
		String packageType = "";
		if (entity.getProjectId().getSingleEntity() != null) {
			packageType = "BASIC";
		} else {
			if (entity.getProjectId().getTripleEntity().getBasicPrice() == entity.getOrderPrice()) {
				packageType = "BASIC";
			} else if (entity.getProjectId().getTripleEntity().getEconomyPrice() == entity.getOrderPrice()) {
				packageType = "ECONOMY";
			} else {
				packageType = "PREMIUM";
			}
		}
		return packageType;
	}

	@Override
	public List<ProjectOrdersResponse.Response> selectOrderListForBuyer(String buyerId, int pageNo) {
		PageRequest pageRequest = PageRequest.of(pageNo, 5, Sort.by(Sort.Direction.DESC, "orderCommitDate"));
		List<ProjectOrdersEntity> orderList = projectOrderRepository.findAllByBuyerId(pageRequest, buyerId);
		List<ProjectOrdersResponse.Response> requestOrderList = new ArrayList<>();

		for (ProjectOrdersEntity entity : orderList) {
			if ("주문대기중".equals(entity.getOrderStatus()) || "주문취소".equals(entity.getOrderStatus())) {
				requestOrderList.add(new ProjectOrdersResponse.Response(entity, getTotalPrice(entity),
						getPackageType(entity), null, null));
			} else if ("진행중".equals(entity.getOrderStatus())) {
				requestOrderList.add(new ProjectOrdersResponse.Response(entity, getTotalPrice(entity),
						getPackageType(entity), entity.getOrderDetail().getOrderDate(), null));
			} else {// "구매확정"

				requestOrderList
						.add(new ProjectOrdersResponse.Response(entity, getTotalPrice(entity), getPackageType(entity),
								entity.getOrderDetail().getOrderDate(), entity.getOrderDetail().getCompletionDate()));
			}
		}

		return requestOrderList;
	}

	@Override
	public List<ProjectOrdersResponse.Response> selectOrderListForBuyer(String buyerId, String category1,
			String category2, LocalDateTime category4, LocalDateTime category5, int pageNo) {
		List<ProjectOrdersEntity> orderList = null;

		PageRequest pageRequest = PageRequest.of(pageNo, 5, Sort.by(Sort.Direction.DESC, "orderCommitDate"));
		if ("all".equals(category1) && !"all".equals(category1)) {
			orderList = projectOrderRepository.selectOrderForBuyerWithoutC1(pageRequest, buyerId, category2, category4,
					category5);
		} else if (!"all".equals(category1) && "all".equals(category2)) {
			orderList = projectOrderRepository.selectOrderForBuyerWithoutC2(pageRequest, buyerId, category1, category4,
					category5);
		} else if ("all".equals(category1) && "all".equals(category2)) {
			orderList = projectOrderRepository.selectOrderForBuyerWithoutC1AndC2(pageRequest, buyerId, category4,
					category5);
		} else {
			orderList = projectOrderRepository.selectOrderForBuyer(pageRequest, buyerId, category1, category2,
					category4, category5);
		}

//		  엔티티 잘 받아 오는지 확인
//		for (ProjectOrdersEntity entity : orderList) {
//			System.out.println("구매자용entity : " + entity);
//		}

		List<ProjectOrdersResponse.Response> requestOrderList = new ArrayList<>();

		for (ProjectOrdersEntity entity : orderList) {
			if ("주문대기중".equals(entity.getOrderStatus()) || "주문취소".equals(entity.getOrderStatus())) {
				requestOrderList.add(new ProjectOrdersResponse.Response(entity, getTotalPrice(entity),
						getPackageType(entity), null, null));
			} else if ("진행중".equals(entity.getOrderStatus())) {
				requestOrderList.add(new ProjectOrdersResponse.Response(entity, getTotalPrice(entity),
						getPackageType(entity), entity.getOrderDetail().getOrderDate(), null));
			} else {// "구매확정"
				requestOrderList
						.add(new ProjectOrdersResponse.Response(entity, getTotalPrice(entity), getPackageType(entity),
								entity.getOrderDetail().getOrderDate(), entity.getOrderDetail().getCompletionDate()));
			}
		}
		return requestOrderList;
	}

	////////// 판매자용/////////////////////////////////////////////////////////////
	@Override // 에셋주문 판매관리 (카테고리X)
	public List<ProjectOrdersResponse.Response> selectOrderListForSeller(String creatorId, int pageNo) {
		PageRequest pageRequest = PageRequest.of(pageNo, 5, Sort.by(Sort.Direction.DESC, "orderCommitDate"));
		List<ProjectOrdersEntity> orderList = projectOrderRepository.findAllByCreatorId(pageRequest, creatorId);

		List<ProjectOrdersResponse.Response> requestOrderList = new ArrayList<>();

		for (ProjectOrdersEntity entity : orderList) {
			if ("주문대기중".equals(entity.getOrderStatus()) || "주문취소".equals(entity.getOrderStatus())) {
				requestOrderList.add(new ProjectOrdersResponse.Response(entity, getTotalPrice(entity),
						getPackageType(entity), null, null));
			} else if ("진행중".equals(entity.getOrderStatus())) {
				requestOrderList.add(new ProjectOrdersResponse.Response(entity, getTotalPrice(entity),
						getPackageType(entity), entity.getOrderDetail().getOrderDate(), null));
			} else {// "구매확정"

				requestOrderList
						.add(new ProjectOrdersResponse.Response(entity, getTotalPrice(entity), getPackageType(entity),
								entity.getOrderDetail().getOrderDate(), entity.getOrderDetail().getCompletionDate()));
			}
		}

		return requestOrderList;
	}

	@Override
	public List<ProjectOrdersResponse.Response> selectOrderListForSeller(String creatorId, String category1,
			String category2, LocalDateTime category4, LocalDateTime category5, int pageNo) {
		List<ProjectOrdersEntity> orderList = null;

		PageRequest pageRequest = PageRequest.of(pageNo, 5, Sort.by(Sort.Direction.DESC, "orderCommitDate"));
		if ("all".equals(category1) && !"all".equals(category1)) {
			orderList = projectOrderRepository.selectOrderForSellerWithoutC1(pageRequest, creatorId, category2,
					category4, category5);
		} else if (!"all".equals(category1) && "all".equals(category2)) {
			orderList = projectOrderRepository.selectOrderForSellerWithoutC2(pageRequest, creatorId, category1,
					category4, category5);
		} else if ("all".equals(category1) && "all".equals(category2)) {
			orderList = projectOrderRepository.selectOrderForSellerWithoutC1AndC2(pageRequest, creatorId, category4,
					category5);
		} else {
			orderList = projectOrderRepository.selectOrderForSeller(pageRequest, creatorId, category1, category2,
					category4, category5);
		}
//		 // 엔티티 잘 받아 오는지 확인
//		for (ProjectOrdersEntity entity : orderList) {
//			System.out.println("판매자용entity : " + entity);
//		}

		List<ProjectOrdersResponse.Response> requestOrderList = new ArrayList<>();

		for (ProjectOrdersEntity entity : orderList) {
			if ("주문대기중".equals(entity.getOrderStatus()) || "주문취소".equals(entity.getOrderStatus())) {
				requestOrderList.add(new ProjectOrdersResponse.Response(entity, getTotalPrice(entity),
						getPackageType(entity), null, null));
			} else if ("진행중".equals(entity.getOrderStatus())) {
				requestOrderList.add(new ProjectOrdersResponse.Response(entity, getTotalPrice(entity),
						getPackageType(entity), entity.getOrderDetail().getOrderDate(), null));
			} else {// "구매확정"
				requestOrderList
						.add(new ProjectOrdersResponse.Response(entity, getTotalPrice(entity), getPackageType(entity),
								entity.getOrderDetail().getOrderDate(), entity.getOrderDetail().getCompletionDate()));
			}
		}
		return requestOrderList;
	}

///////////////판매자 & 구매자 공용///////////////////////////////////////////////////
	// 구매확정 업데이트
	@Override
	@Transactional
	public void updateOrder(ProjectOrdersConfirmUpdateDTO updatedData) {

		ProjectOrdersEntity orderEntity = dao.selectOrderById(updatedData.getOrderId())
				.orElseThrow(() -> new RuntimeException());
		if ("진행중".equals(updatedData.getOrderStatus())) {
			orderEntity.getOrderDetail().setCompletionDate(LocalDateTime.now());
			orderEntity.setOrderStatus("구매확정");
		} else {
			if ("accept".equals(updatedData.getAcceptanceValue())) {
				// 부모 채우기
				orderEntity.setOrderStatus("진행중");
				// 자식 채우기
				ProjectOrdersDetailEntity orderDetail = new ProjectOrdersDetailEntity();
				orderDetail.setOrderDate(LocalDateTime.now());
				orderDetail.setOrderId(orderEntity);
				// 최종 부모자식 객체 싱크맞추기
				orderEntity.setOrderDetail(orderDetail);

			} else {
				orderEntity.setOrderStatus("주문취소");
			}
		}
	}
///////////////에셋 주문////////////////////////////

	@Override//에셋 주문 불러오기 - 구매자용
	public List<AssetOrdersResponse.Response> selectAssetOrderListForBuyer(String buyerId, int pageNo) {
		PageRequest pageRequest = PageRequest.of(pageNo, 5, Sort.by(Sort.Direction.DESC, "assetOrderDate"));
		List<AssetOrdersEntity> orderList = assetOrderRepository.findAllByBuyerId2(pageRequest, buyerId);
		List<AssetOrdersResponse.Response> requestOrderList = new ArrayList<>();
		for(AssetOrdersEntity entity : orderList) {
			requestOrderList.add(new AssetOrdersResponse.Response(entity));
		}
		
		return requestOrderList;
	}

	@Override//에셋 주문 불러오기 (카테고리) - 구매자용
	public List<AssetOrdersResponse.Response> selectAssetOrderListForBuyer(String buyerId, String category1, String category2,
			LocalDateTime category4, LocalDateTime category5, int pageNo) {
		List<AssetOrdersEntity> orderList = null;

		PageRequest pageRequest = PageRequest.of(pageNo, 5, Sort.by(Sort.Direction.DESC, "assetOrderDate"));
		if ("all".equals(category1) && !"all".equals(category1)) {
			orderList = assetOrderRepository.selectOrderForBuyerWithoutC1(pageRequest, buyerId, category2, category4,
					category5);
		} else if (!"all".equals(category1) && "all".equals(category2)) {
			orderList = assetOrderRepository.selectOrderForBuyerWithoutC2(pageRequest, buyerId, category1, category4,
					category5);
		} else if ("all".equals(category1) && "all".equals(category2)) {
			orderList = assetOrderRepository.selectOrderForBuyerWithoutC1AndC2(pageRequest, buyerId, category4,
					category5);
		} else {
			orderList = assetOrderRepository.selectOrderForBuyer(pageRequest, buyerId, category1, category2,
					category4, category5);
		}
		List<AssetOrdersResponse.Response> requestOrderList = new ArrayList<>();
		for(AssetOrdersEntity entity : orderList) {
			requestOrderList.add(new AssetOrdersResponse.Response(entity));
			System.out.println("오더서비스 호출됨");
			System.out.println("오더서비스 orderList : " + entity);
		}
		
		return requestOrderList;
	}

	@Override
	public List<AssetOrdersResponse.Response> selectAssetOrderListForSeller(String sellerId, int pageNo) {
		PageRequest pageRequest = PageRequest.of(pageNo, 5, Sort.by(Sort.Direction.DESC, "assetOrderDate"));
		List<AssetOrdersEntity> orderList = assetOrderRepository.findAllBySellerId(pageRequest, sellerId);
		List<AssetOrdersResponse.Response> requestOrderList = new ArrayList<>();

		for(AssetOrdersEntity entity : orderList) {
			requestOrderList.add(new AssetOrdersResponse.Response(entity));
		}
		return requestOrderList;
	}

	@Override
	public List<AssetOrdersResponse.Response> selectAssetOrderListForSeller(String buyerId, String category1, String category2,
			LocalDateTime category4, LocalDateTime category5, int pageNo) {
		List<AssetOrdersEntity> orderList = null;

		PageRequest pageRequest = PageRequest.of(pageNo, 5, Sort.by(Sort.Direction.DESC, "assetOrderDate"));
		if ("all".equals(category1) && !"all".equals(category1)) {
			orderList = assetOrderRepository.selectOrderForSellerWithoutC1(pageRequest, buyerId, category2, category4,
					category5);
		} else if (!"all".equals(category1) && "all".equals(category2)) {
			orderList = assetOrderRepository.selectOrderForSellerWithoutC2(pageRequest, buyerId, category1, category4,
					category5);
		} else if ("all".equals(category1) && "all".equals(category2)) {
			orderList = assetOrderRepository.selectOrderForSellerWithoutC1AndC2(pageRequest, buyerId, category4,
					category5);
		} else {
			orderList = assetOrderRepository.selectOrderForSeller(pageRequest, buyerId, category1, category2,
					category4, category5);
		}
		List<AssetOrdersResponse.Response> requestOrderList = new ArrayList<>();
		for(AssetOrdersEntity entity : orderList) {
			requestOrderList.add(new AssetOrdersResponse.Response(entity));
			
		}
		
		return requestOrderList;
	}

}
