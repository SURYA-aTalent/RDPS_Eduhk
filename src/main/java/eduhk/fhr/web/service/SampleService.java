package eduhk.fhr.web.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import eduhk.fhr.web.dao.SampleDao;
import eduhk.fhr.web.model.SampleBooking;
import eduhk.fhr.web.util.Common;

@Service
@Transactional(rollbackFor = Exception.class)
public class SampleService extends BaseService {
	
	@Autowired
	private SampleDao sampleDao;
	
	public ArrayList<SampleBooking> getBookingList() {
		return sampleDao.getBookingList();
	}
	
	@Transactional
	public SampleBooking saveSampleReservation(SampleBooking booking) {
		return sampleDao.saveSampleReservation(booking);
	}
	
	@Transactional
	public String updateSampleReservationBookingAckDate(SampleBooking booking) {
		return sampleDao.updateSampleReservationBookingAckDate(booking);
	}
	
	public SampleBooking searchSampleBooking(SampleBooking booking) {
		return sampleDao.searchSampleBooking(booking);
	}
	
	public int getAlreadyApplied(SampleBooking booking) {
		return sampleDao.getAlreadyApplied(booking);
	}
	
	@Transactional
	public String updateSampleReservationCancelAckDate(SampleBooking booking) {
		return sampleDao.updateSampleReservationCancelAckDate(booking);
	}
	
}
