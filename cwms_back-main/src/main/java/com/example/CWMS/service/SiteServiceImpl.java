package com.example.CWMS.service;

import com.example.CWMS.iservice.SiteService;
import com.example.CWMS.model.Site;
import com.example.CWMS.repository.SiteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SiteServiceImpl implements SiteService {

    private final SiteRepository siteRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Site> getAllSites() {
        return siteRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Site getSiteById(Integer id) {
        return siteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Site non trouvé avec l'id : " + id));
    }

    @Override
    @Transactional
    public Site createSite(Site site) {
        site.setCreatedAt(new Date());
        return siteRepository.save(site);
    }

    @Override
    @Transactional
    public Site updateSite(Integer id, Site siteDetails) {
        Site site = getSiteById(id);
        site.setSiteName(siteDetails.getSiteName());
        site.setUpdatedAt(new Date());
        return siteRepository.save(site);
    }

    @Override
    @Transactional
    public void deleteSite(Integer id) {
        siteRepository.delete(getSiteById(id));
    }
}