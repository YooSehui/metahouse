package com.multi.metahouse.review.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.multi.metahouse.domain.dto.review.ReviewDTO;
import com.multi.metahouse.review.repository.dao.ReviewDAO;

public class ReviewServiceImpl implements ReviewService {

	private ReviewDAO reviewDAO;

    @Autowired
    public ReviewServiceImpl(ReviewDAO reviewDAO) {
        this.reviewDAO = reviewDAO;
    }

    @Override
    public ReviewDTO getReviewById(int reviewId) {
        return reviewDAO.getReviewById(reviewId);
    }

    @Override
    public List<ReviewDTO> getAllReviews() {
        return reviewDAO.getAllReviews();
    }

    @Override
    public void createReview(ReviewDTO reviewDTO) {
        reviewDAO.createReview(reviewDTO);
    }

    @Override
    public void updateReview(ReviewDTO reviewDTO) {
        reviewDAO.updateReview(reviewDTO);
    }

    @Override
    public void deleteReview(int reviewId) {
        reviewDAO.deleteReview(reviewId);
    }
}