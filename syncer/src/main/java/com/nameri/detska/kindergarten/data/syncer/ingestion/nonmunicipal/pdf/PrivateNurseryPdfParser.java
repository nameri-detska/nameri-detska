package com.nameri.detska.kindergarten.data.syncer.ingestion.nonmunicipal.pdf;

import java.util.List;

import com.nameri.detska.kindergarten.data.syncer.ingestion.KidFacilityDto;

/**
 * Parses a PDF file containing information about private nurseries.
 */
public interface PrivateNurseryPdfParser {

    /**
     * Parses a PDF file containing information about private nurseries.
     *
     * @param pdfBytes the bytes of the PDF file
     * @return the list of parsed kid facilities
     */
    List<KidFacilityDto> parse(byte[] pdfBytes);
}
