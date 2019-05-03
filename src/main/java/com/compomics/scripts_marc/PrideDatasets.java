package com.compomics.scripts_marc;


/**
 *
 * @author Marc Vaudel
 */
public class PrideDatasets {
//
//    /**
//     * The web service URL.
//     */
//    private static final String PROJECT_SERVICE_URL = "https://www.ebi.ac.uk/pride/ws/archive/";
//
//    /**
//     * Main method.
//     *
//     * @param args the command line arguments
//     */
//    public static void main(String[] args) {
//
//        try {
//            
//            Dummy dummy = new Dummy();
//
//            String outputFileStem = "C:\\Users\\mvaudel\\Documents\\grants\\2019\\NFR\\figures\\pride\\data.gz";
//
//            SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
//            SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
//
//            HashMap<String, ArrayList<String>> reshakeableFiles = new HashMap<>();
//
//            // add pride xml and mgf
//            reshakeableFiles.put("PEAK", new ArrayList<>());
//            reshakeableFiles.get("PEAK").add(MsFormat.mgf.fileNameEnding);
//            reshakeableFiles.get("PEAK").add(MsFormat.mgf.fileNameEnding + ".gz");
//            reshakeableFiles.get("PEAK").add(MsFormat.mgf.fileNameEnding + ".zip");
//
//            // add the raw file formats
//            reshakeableFiles.put("RAW", new ArrayList<>());
//            reshakeableFiles.get("RAW").add(MsFormat.raw.fileNameEnding);
//            reshakeableFiles.get("RAW").add(MsFormat.raw.fileNameEnding + ".gz");
//            reshakeableFiles.get("RAW").add(MsFormat.raw.fileNameEnding + ".zip");
//            reshakeableFiles.get("RAW").add(MsFormat.mzML.fileNameEnding);
//            reshakeableFiles.get("RAW").add(MsFormat.mzML.fileNameEnding + ".gz");
//            reshakeableFiles.get("RAW").add(MsFormat.mzML.fileNameEnding + ".zip");
//            reshakeableFiles.get("RAW").add(MsFormat.mzXML.fileNameEnding);
//            reshakeableFiles.get("RAW").add(MsFormat.mzXML.fileNameEnding + ".gz");
//            reshakeableFiles.get("RAW").add(MsFormat.mzXML.fileNameEnding + ".zip");
//            reshakeableFiles.get("RAW").add(MsFormat.baf.fileNameEnding);
//            reshakeableFiles.get("RAW").add(MsFormat.baf.fileNameEnding + ".gz");
//            reshakeableFiles.get("RAW").add(MsFormat.baf.fileNameEnding + ".zip");
//            reshakeableFiles.get("RAW").add(MsFormat.fid.fileNameEnding);
//            reshakeableFiles.get("RAW").add(MsFormat.fid.fileNameEnding + ".gz");
//            reshakeableFiles.get("RAW").add(MsFormat.fid.fileNameEnding + ".zip");
//            reshakeableFiles.get("RAW").add(MsFormat.yep.fileNameEnding);
//            reshakeableFiles.get("RAW").add(MsFormat.yep.fileNameEnding + ".gz");
//            reshakeableFiles.get("RAW").add(MsFormat.yep.fileNameEnding + ".zip");
//            reshakeableFiles.get("RAW").add(MsFormat.d.fileNameEnding);
//            reshakeableFiles.get("RAW").add(MsFormat.d.fileNameEnding + ".gz");
//            reshakeableFiles.get("RAW").add(MsFormat.d.fileNameEnding + ".zip");
//            reshakeableFiles.get("RAW").add(MsFormat.wiff.fileNameEnding); // @TODO: also requries the corresponding .scan file...
//            reshakeableFiles.get("RAW").add(MsFormat.wiff.fileNameEnding + ".gz");
//            reshakeableFiles.get("RAW").add(MsFormat.wiff.fileNameEnding + ".zip");
//            reshakeableFiles.get("RAW").add(MsFormat.mz5.fileNameEnding);
//
//            // then check for incorrect labeling...
//            reshakeableFiles.put("OTHER", new ArrayList<>());
//            reshakeableFiles.get("OTHER").add(MsFormat.mgf.fileNameEnding);
//            reshakeableFiles.get("OTHER").add(MsFormat.mgf.fileNameEnding + ".gz");
//            reshakeableFiles.get("OTHER").add(MsFormat.mgf.fileNameEnding + ".zip");
//            reshakeableFiles.get("RAW").add(MsFormat.mgf.fileNameEnding);
//            reshakeableFiles.get("RAW").add(MsFormat.mgf.fileNameEnding + ".gz");
//            reshakeableFiles.get("RAW").add(MsFormat.mgf.fileNameEnding + ".zip");
//
//            String projectCountUrl = PROJECT_SERVICE_URL + "project/count";
//            RestTemplate template = new RestTemplate();
//            ResponseEntity<Integer> projectCountResult = template.getForEntity(projectCountUrl, Integer.class); // can also use project/count/?q=*
//            int numberOfProjects = projectCountResult.getBody();
//
//            int projectBatchSize = 100;
//            int numberOfPages = (int) Math.ceil(((double) numberOfProjects) / projectBatchSize);
//
//            // load the projects in batches
//            for (int currentPage = 50; currentPage < numberOfPages; currentPage++) {
//
//                File outputFile = new File(outputFileStem + "_" + currentPage + ".gz");
//
//                try (SimpleFileWriter writer = new SimpleFileWriter(outputFile, true)) {
//
//                    writer.writeLine(
//                            "accession",
//                            "species",
//                            "tissues",
//                            "mods",
//                            "instruments",
//                            "year",
//                            "month",
//                            "time",
//                            "nFiles",
//                            "sizeGb"
//                    );
//
//                    // get the list of projects
//                    ResponseEntity<ProjectDetailList> projectList = template.getForEntity(PROJECT_SERVICE_URL
//                            + "project/list?show=" + projectBatchSize + "&page=" + currentPage + "&sort=publication_date&order=desc", ProjectDetailList.class);
//
//                    // iterate the project and add them to the output
//                    for (ProjectDetail projectDetail : projectList.getBody().getList()) {
//
//                        String projectAccession = projectDetail.getAccession();
//
//                        try {
//                            
//                        String species = projectDetail.getSpecies().stream()
//                                .sorted()
//                                .collect(Collectors.joining(","));
//                        String tissues = projectDetail.getTissues().stream()
//                                .sorted()
//                                .collect(Collectors.joining(","));
//                        String ptms = projectDetail.getPtmNames().stream()
//                                .sorted()
//                                .collect(Collectors.joining(","));
//                        String instruments = projectDetail.getInstrumentNames().stream()
//                                .sorted()
//                                .collect(Collectors.joining(","));
//
//                        String yearString = yearFormat.format(projectDetail.getPublicationDate());
//                        String monthString = monthFormat.format(projectDetail.getPublicationDate());
//
//                        double year = Double.parseDouble(yearString);
//                        double month = Double.parseDouble(monthString);
//                        double time = year + ((month - 1) / 12);
//
//                        String accessionUrl = PROJECT_SERVICE_URL + "file/list/project/" + projectAccession;
//                        
//                        ResponseEntity<FileDetailList> fileDetailListResult = template.getForEntity(accessionUrl, FileDetailList.class);
//
//                        HashMap<String, Double> msFiles = new HashMap<>();
//
//                        for (FileDetail fileDetail : fileDetailListResult.getBody().getList()) {
//
//                            String name = null;
//
//                            // check if the file is reshakeable
//                            String fileType = fileDetail.getFileType().getName();
//                            if (reshakeableFiles.containsKey(fileType)) {
//                                for (String fileEnding : reshakeableFiles.get(fileType)) {
//
//                                    String fileName = fileDetail.getFileName().toLowerCase();
//
//                                    if (fileName.endsWith(fileEnding)) {
//
//                                        name = fileName.substring(0, fileName.length() - fileEnding.length());
//
//                                        break;
//
//                                    }
//                                }
//                            }
//
//                            if (name != null) {
//
//                                double fileSize = ((double) fileDetail.getFileSize()) / 1073741824;
//
//                                if (!msFiles.containsKey(name) || msFiles.get(name) < fileSize) {
//
//                                    msFiles.put(name, fileSize);
//
//                                }
//                            }
//                        }
//
//                        int nFiles = msFiles.size();
//                        double sizeGb = msFiles.values().stream().mapToDouble(a -> a).sum();
//
//                        writer.writeLine(
//                                projectAccession,
//                                species,
//                                tissues,
//                                ptms,
//                                instruments,
//                                yearString,
//                                monthString,
//                                Double.toString(time),
//                                Integer.toString(nFiles),
//                                Double.toString(sizeGb)
//                        );
//                        
//                        } catch (HttpClientErrorException e) {
//                            System.out.println("Failed: " + projectAccession + " (501)");
//                        } catch (HttpServerErrorException e) {
//                            System.out.println("Failed: " + projectAccession + " (500)");
//                        }
//
//                    }
//
//                    System.out.println(currentPage + "/" + numberOfPages);
//                    
//                    dummy.delay(60000);
//
//                }
//            }
//
//        } catch (Throwable t) {
//            t.printStackTrace();
//        }
//    }

}
